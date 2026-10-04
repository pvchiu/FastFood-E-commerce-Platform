package com.example.myproject.controller;

import com.example.myproject.service.CategoryService;
import com.example.myproject.service.OrderService;
import com.example.myproject.service.ProductService;
import com.example.myproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerPageController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/product-management")
    public String showProductManagementPage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/product-management";
    }

    @GetMapping("/order-management")
    public String showOrderManagementPage(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/order-management";
    }

    @GetMapping("/order-details")
    public String showOrderDetailsPage() {
        return "admin/order-details";
    }
}
