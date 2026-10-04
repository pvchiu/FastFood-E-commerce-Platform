package com.example.myproject.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.myproject.entity.CartItem;
import com.example.myproject.entity.User;
import com.example.myproject.repository.CartItemRepository;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.service.CartItemService;

@Controller
public class CartController {

    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cart")
    public String viewCart(Model model, Principal principal) {
        String username = principal.getName();

        List<CartItem> cartItems = cartItemService.getCartItemsByUsername(username);

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = new BigDecimal("1.2");
        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discount);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartItemCount", cartItems.size());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("discount", discount);
        model.addAttribute("totalAmount", totalAmount);

        return "/cart/view";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            Principal principal) {

        if (principal != null) {
            cartItemService.addToCart(principal.getName(), productId, quantity);
            return ResponseEntity.ok("Added to cart");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public String updateQuantity(@RequestParam Long productId,
            @RequestParam int quantity,
            Principal principal) {
        if (quantity < 1)
            return "invalid";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null)
            return "unauthorized";

        CartItem item = cartItemRepository.findByUserAndProductId(user, productId).orElse(null);
        if (item == null)
            return "not_found";

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return "ok";
    }

    @DeleteMapping("/cart/remove")
    @ResponseBody
    public String removeItem(@RequestParam Long productId, Principal principal) {
        try {
            if (principal == null) {
                return "unauthorized";
            }

            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user == null) {
                return "unauthorized";
            }

            CartItem item = cartItemRepository.findByUserAndProductId(user, productId).orElse(null);
            if (item == null) {
                return "not_found";
            }

            cartItemRepository.delete(item);
            return "ok";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}