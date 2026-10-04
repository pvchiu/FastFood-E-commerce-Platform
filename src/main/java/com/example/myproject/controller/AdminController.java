package com.example.myproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.myproject.entity.User;
import com.example.myproject.entity.User.UserStatus;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Admin/Manager pages.
 */
@Controller
public class AdminController {
   @Autowired
   private UserService userService;
   @Autowired
   private UserRepository userRepository;

   /**
    * Returns the order management interface for admin/manager.
    * Path: /admin/order-management
    */
   @GetMapping("/admin/order-management")
   public String orderManagementPage(Model model) {
      // Add empty orders list for now - will be populated when backend is integrated
      model.addAttribute("orders", new ArrayList<>());
      return "admin/order-management";
   }

   /**
    * Returns the product management interface for admin/manager.
    * Path: /admin/product-management
    */
   @GetMapping("/admin/product-management")
   public String productManagementPage(Model model) {
      // Add empty products list for now - will be populated via AJAX from API
      model.addAttribute("products", new ArrayList<>());
      return "admin/product-management";
   }

   @GetMapping("/admin/user-management")
   public String listUsers(
         @RequestParam(required = false) String keyword,
         @RequestParam(required = false) String status,
         Model model) {

      UserStatus userStatus = null;

      if (status != null && !status.isEmpty()) {
         try {
            userStatus = UserStatus.valueOf(status.toUpperCase());
         } catch (IllegalArgumentException e) {
            userStatus = null;
         }
      }

      List<User> users;
      if ((keyword == null || keyword.isBlank()) && userStatus == null) {
         users = userRepository.findAll();
      } else if (userStatus != null && (keyword == null || keyword.isBlank())) {
         users = userRepository.findByStatus(userStatus);
      } else if (userStatus == null) {
         users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
      } else {
         users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(keyword, keyword,
               userStatus);
      }
      long totalUsers = userRepository.count();
      long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
      long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);

      model.addAttribute("users", users);
      model.addAttribute("keyword", keyword);
      model.addAttribute("status", status);
      model.addAttribute("statuses", User.UserStatus.values());
      model.addAttribute("totalUsers", totalUsers);
      model.addAttribute("activeUsers", activeUsers);
      model.addAttribute("inactiveUsers", inactiveUsers);

      return "admin/user-management";
   }

   @PostMapping("/admin/users/toggle/{id}")
   public String toggleUserStatus(@PathVariable("id") Long id) {
      userService.toggleUserStatus(id);
      return "redirect:/admin/user-management";
   }

}
