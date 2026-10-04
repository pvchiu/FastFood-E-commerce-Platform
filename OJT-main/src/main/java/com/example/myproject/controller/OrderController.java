package com.example.myproject.controller;

import com.example.myproject.entity.Order;
import com.example.myproject.entity.OrderItem;
import com.example.myproject.entity.Product;
import com.example.myproject.entity.Review;
import com.example.myproject.entity.User;
import com.example.myproject.repository.ReviewRepository;
import com.example.myproject.service.CartItemService;
import com.example.myproject.service.OrderService;
import com.example.myproject.service.ProductService;
import com.example.myproject.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public String viewOrderHistory(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getId();

        List<Order> orders = orderService.findByUserIdOrderByCreatedAtDesc(userId);
        List<Long> reviewedProductIds = reviewRepository.findByUser(user)
                .stream()
                .map(review -> review.getProduct().getId())
                .collect(Collectors.toList());
        model.addAttribute("reviewedProductIds", reviewedProductIds);
        model.addAttribute("orders", orders);

        model.addAttribute("totalSpent", orderService.getTotalSpentByUser(userId));

        return "order-history";
    }

    @GetMapping("/reviews/create")
    public String showReviewForm(@RequestParam(required = false) Long orderId, Model model) {
        if (orderId == null) {
            return "redirect:/order-history"; // hoặc trả về trang lỗi
        }

        Order order = orderService.findById(orderId);
        List<OrderItem> items = orderService.findOrderItemsByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "review-form"; // tên file HTML đúng như bạn đã tạo
    }

    @PostMapping("/reviews/submit")
    public String submitReview(@RequestParam Long orderId,
            @RequestParam Long productId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        // 1. Lấy user từ session
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Lấy product từ service
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 3. Tạo đối tượng review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsVisible(true); // Set visible by default
        review.setCreatedAt(LocalDateTime.now());

        // 4. Lưu vào DB
        reviewRepository.save(review);

        // 5. Thông báo và chuyển hướng
        redirectAttributes.addFlashAttribute("message", "Review submitted successfully!");

        return "redirect:/orders"; // Hoặc redirect về order history
    }

}
