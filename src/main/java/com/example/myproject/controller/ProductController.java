package com.example.myproject.controller;

import com.example.myproject.entity.Product;
import com.example.myproject.entity.Review;
import com.example.myproject.entity.User;
import com.example.myproject.repository.CategoryRepository;
import com.example.myproject.repository.ReviewRepository;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.service.ProductService;
import com.example.myproject.service.ReviewService;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.apache.commons.collections4.ListUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/products")
    public String showProductList(
            @RequestParam(value = "categories", required = false) List<Long> categoryIds,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "keyword", required = false) String keyword, // THÊM DÒNG NÀY
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection,
            @RequestParam(value = "priceRange", required = false) String priceRange,
            Model model) {

        if (priceRange != null && !priceRange.isEmpty()) {
            switch (priceRange) {
                case "under2$":
                    minPrice = BigDecimal.ZERO;
                    maxPrice = new BigDecimal("2");
                    break;
                case "2$-5$":
                    minPrice = new BigDecimal("2");
                    maxPrice = new BigDecimal("5");
                    break;
                case "above5$":
                    minPrice = new BigDecimal("5");
                    maxPrice = null;
                    break;
            }
        }

        if (sortBy.equals("default")) {
            sortBy = "createdAt";
        }

        Pageable pageable = PageRequest.of(
                page, size,
                sortDirection.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProducts(keyword.trim(), pageable);
            model.addAttribute("searchKeyword", keyword);
        } else {
            productPage = productService.getFilteredAndSortedProducts(
                    categoryIds, minPrice, maxPrice, pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategories", categoryIds);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("selectedSortDirection", sortDirection);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String viewProductDetail(@PathVariable("id") Long id, Model model, Principal principal) {
        try {
            return productService.getProductById(id)
                    .map(product -> {
                        model.addAttribute("product", product);
                        List<Review> reviews = reviewService.getReviewsByProduct(product);
                        model.addAttribute("reviews", reviews);
                        double avgRating = reviews.stream()
                                .mapToInt(Review::getRating)
                                .average()
                                .orElse(0.0);
                        model.addAttribute("averageRating", avgRating);
                        long totalReviews = reviews.size();
                        model.addAttribute("reviewCount", totalReviews);

                        for (int i = 1; i <= 5; i++) {
                            final int star = i;
                            long count = reviews.stream().filter(r -> r.getRating() == star).count();
                            int percentage = (totalReviews == 0) ? 0 : (int) ((count * 100.0) / totalReviews);
                            model.addAttribute("star" + i + "Percent", percentage);
                        }

                        // Check if user has already reviewed this product
                        if (principal != null) {
                            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                                reviewRepository.findByUserAndProduct(user, product).ifPresent(review -> {
                                    model.addAttribute("hasReviewed", true);
                                    model.addAttribute("userReview", review);
                                });
                            });
                            model.addAttribute("isAuthenticated", true);
                        } else {
                            model.addAttribute("isAuthenticated", false);
                        }

                        return "products/detail";
                    })
                    .orElse("redirect:/products");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/products";
        }
    }

    @GetMapping("/detail")
    public String showProductDetail() {
        return "products/detail";
    }

    @GetMapping("/products/{id}/test")
    public String viewProductDetailTest(@PathVariable("id") Long id, Model model, Principal principal) {
        try {
            return productService.getProductById(id)
                    .map(product -> {
                        model.addAttribute("product", product);
                        List<Review> reviews = reviewService.getReviewsByProduct(product);
                        model.addAttribute("reviewCount", reviews.size());
                        double avgRating = reviews.stream()
                                .mapToInt(Review::getRating)
                                .average()
                                .orElse(0.0);
                        model.addAttribute("averageRating", avgRating);

                        if (principal != null) {
                            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                                boolean hasReviewed = reviewRepository.existsByUserAndProduct(user, product);
                                model.addAttribute("hasReviewed", hasReviewed);
                            });
                            model.addAttribute("isAuthenticated", true);
                        } else {
                            model.addAttribute("isAuthenticated", false);
                        }

                        return "products/detail-test";
                    })
                    .orElse("redirect:/products");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/products";
        }
    }

    @PostMapping("/products/{id}/reviews")
    public String submitProductReview(@PathVariable("id") Long productId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra user đã đăng nhập
            if (principal == null) {
                redirectAttributes.addFlashAttribute("error", "Please login to review this product!");
                return "redirect:/login";
            }

            // Lấy user từ database
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy product từ database
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Kiểm tra user đã review sản phẩm này chưa
            boolean hasReviewed = reviewRepository.existsByUserAndProduct(user, product);
            if (hasReviewed) {
                redirectAttributes.addFlashAttribute("error", "You have already reviewed this product!");
                return "redirect:/products/" + productId;
            }

            // Tạo review mới
            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);
            review.setIsVisible(true); // Set visible by default
            review.setCreatedAt(LocalDateTime.now());

            // Lưu vào database
            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("success", "Thank you for your review!");
            return "redirect:/products/" + productId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while submitting your review: " + e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    // @GetMapping("/products/search")
    // public String searchProducts(@RequestParam("keyword") String keyword,
    // @RequestParam(value = "source", defaultValue = "index") String source,
    // Model model) {
    // List<Product> products = productService.searchProducts(keyword);
    // model.addAttribute("products", products);
    // model.addAttribute("searchKeyword", keyword);
    // model.addAttribute("searchSource", source);

    // if ("list".equals(source)) {
    // return "products/list";
    // } else {
    // List<List<Product>> productGroups = ListUtils.partition(products, 3);
    // model.addAttribute("productGroups", productGroups);
    // return "index";
    // }
    // }

}