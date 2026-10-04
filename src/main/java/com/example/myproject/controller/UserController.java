package com.example.myproject.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.myproject.entity.Product;
import com.example.myproject.entity.User;
import com.example.myproject.service.ProductService;
import com.example.myproject.service.UserService;
import org.apache.commons.collections4.ListUtils;
import com.example.myproject.dto.UserRegistrationDto;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.example.myproject.repository.CartItemRepository cartItemRepository;

    @Autowired
    private com.example.myproject.repository.UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", authentication.getName());

            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                int cartCount = cartItemRepository.findByUser(user).size();
                model.addAttribute("cartItemCount", cartCount);
            });
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("cartItemCount", 0);
        }

        // model.addAttribute("products", productService.getAvailableProducts());
        List<Product> products = productService.getAvailableProducts().stream().limit(9).toList();

        List<List<Product>> productGroups = ListUtils.partition(products, 3);

        model.addAttribute("productGroups", productGroups);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto registrationDto,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("error", "Password confirmation does not match!");
            model.addAttribute("user", registrationDto);
            return "register";
        }

        try {
            User user = new User();
            user.setUsername(registrationDto.getUsername());
            user.setEmail(registrationDto.getEmail());
            user.setPassword(userService.encodePassword(registrationDto.getPassword()));
            user.setFullName(registrationDto.getFullName());
            user.setPhoneNumber(registrationDto.getPhoneNumber());
            user.setAddress(registrationDto.getAddress());
            user.setRole(User.Role.CUSTOMER);

            userService.createUser(user);

            return "redirect:/login?success=true";

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Username")) {
                model.addAttribute("error", "Username already exists!");
            } else if (e.getMessage().contains("Email")) {
                model.addAttribute("error", "Email is already in use!");
            } else {
                model.addAttribute("error", "An error occurred during registration!");
            }
            model.addAttribute("user", registrationDto);
            return "register";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty() || optionalUser.get().getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return "redirect:/reset-password?resetError";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email not found!");
            return "redirect:/forgot-password?error";
        }

        User user = optionalUser.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        String resetLink = request.getRequestURL().toString().replace("/forgot-password", "") + "/reset-password?token="
                + token;
        String subject = "Reset Your Password";
        String body = "Click this link to reset your password:\n" + resetLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        redirectAttributes.addFlashAttribute("success", "Reset link sent!");
        return "redirect:/forgot-password?sent";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty())
            return "redirect:/reset-password?resetError";

        User user = optionalUser.get();
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return "redirect:/reset-password?resetError";
        }

        if (newPassword.length() < 8 ||
                !newPassword.matches(".*[a-z].*") ||
                !newPassword.matches(".*[A-Z].*") ||
                !newPassword.matches(".*\\d.*")) {

            redirectAttributes.addFlashAttribute("error",
                    "Password must be at least 8 characters and contain uppercase, lowercase, and a number.");
            return "redirect:/reset-password?token=" + token + "&resetError";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "redirect:/login?resetSuccess";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully!");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {

            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                int cartCount = cartItemRepository.findByUser(user).size();
                model.addAttribute("cartItemCount", cartCount);
                model.addAttribute("user", user);
            });

            model.addAttribute("title", "My Profile");
            return "profile";
        } else {
            return "redirect:/login";
        }
    }

    @PutMapping("/api/profile/update")
    @ResponseBody
    public ResponseEntity<String> updateProfile(@RequestBody User updatedUser, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setAddress(updatedUser.getAddress());

        userRepository.save(user);

        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/api/profile/avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("No file selected");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(400).body("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.status(400).body("File size must be less than 5MB");
        }

        try {
            Path staticImagesDir = Paths.get(System.getProperty("user.dir"),
                    "src", "main", "resources", "static", "images");

            if (!Files.exists(staticImagesDir)) {
                Files.createDirectories(staticImagesDir);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(fileExtension.toLowerCase())) {
                    fileExtension = ".jpg";
                }
            }

            String filename = username + fileExtension;
            Path filePath = staticImagesDir.resolve(filename);

            // Delete old avatar file if exists
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            file.transferTo(filePath.toFile());

            String avatarUrl = "/images/" + filename + "?v=" + System.currentTimeMillis();
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @PutMapping("/api/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> passwordMap,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        String currentPassword = passwordMap.get("currentPassword");
        String newPassword = passwordMap.get("newPassword");

        if (!userService.matchesPassword(currentPassword, user.getPassword())) {
            return ResponseEntity.status(400).body("Current password is incorrect.");
        }

        user.setPassword(userService.encodePassword(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully.");
    }

    @GetMapping("/api/check-username")
    @ResponseBody
    public boolean checkUsernameExists(@RequestParam String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @GetMapping("/feedback-success")
    public String feedbackPage() {
        return "feedback-success";
    }

}
