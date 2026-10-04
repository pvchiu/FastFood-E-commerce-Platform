package com.example.myproject.controller;



import com.example.myproject.entity.CartItem;
import com.example.myproject.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttribute {

    @Autowired
    private CartItemService cartItemService;

    @ModelAttribute
    public void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            List<CartItem> cartItems = cartItemService.getCartItemsByUsername(username);
            model.addAttribute("cartItemCount", cartItems.size());
        } else {
            model.addAttribute("cartItemCount", 0);
        }
    }
}
