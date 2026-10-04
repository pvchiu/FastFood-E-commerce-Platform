package com.example.myproject.controller;

import com.example.myproject.entity.Review;
import com.example.myproject.entity.Product;
import com.example.myproject.repository.ReviewRepository;
import com.example.myproject.repository.ProductRepository;
import com.example.myproject.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

   @Autowired
   private ReviewRepository reviewRepository;

   @Autowired
   private ProductRepository productRepository;

   @Autowired
   private ReviewService reviewService;

   @GetMapping
   public String showReviewManagement(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(required = false) String search,
         @RequestParam(required = false) Integer rating,
         @RequestParam(required = false) Boolean visible,
         @RequestParam(required = false) Long productId,
         Model model) {

      Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
      Page<Review> reviewPage;

      // Apply filters
      if (search != null && !search.trim().isEmpty()) {
         if (rating != null && visible != null && productId != null) {
            reviewPage = reviewRepository.findBySearchTermAndRatingAndIsVisibleAndProductId(
                  search, rating, visible, productId, pageable);
         } else if (rating != null && visible != null) {
            reviewPage = reviewRepository.findBySearchTermAndRatingAndIsVisible(
                  search, rating, visible, pageable);
         } else if (rating != null) {
            reviewPage = reviewRepository.findBySearchTermAndRating(search, rating, pageable);
         } else if (visible != null) {
            reviewPage = reviewRepository.findBySearchTermAndIsVisible(search, visible, pageable);
         } else if (productId != null) {
            reviewPage = reviewRepository.findBySearchTermAndProductId(search, productId, pageable);
         } else {
            reviewPage = reviewRepository.findBySearchTerm(search, pageable);
         }
      } else {
         if (rating != null && visible != null && productId != null) {
            reviewPage = reviewRepository.findByRatingAndIsVisibleAndProductId(
                  rating, visible, productId, pageable);
         } else if (rating != null && visible != null) {
            reviewPage = reviewRepository.findByRatingAndIsVisible(rating, visible, pageable);
         } else if (rating != null) {
            reviewPage = reviewRepository.findByRating(rating, pageable);
         } else if (visible != null) {
            reviewPage = reviewRepository.findByIsVisible(visible, pageable);
         } else if (productId != null) {
            reviewPage = reviewRepository.findByProductId(productId, pageable);
         } else {
            reviewPage = reviewRepository.findAll(pageable);
         }
      }

      // Get all products for filter dropdown
      List<Product> products = productRepository.findAll();

      // Calculate statistics
      long totalReviews = reviewRepository.count();
      long visibleReviews = reviewRepository.countByIsVisible(true);
      long hiddenReviews = reviewRepository.countByIsVisible(false);

      // Calculate average rating
      Double averageRating = reviewRepository.findAll().stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);

      model.addAttribute("reviewPage", reviewPage);
      model.addAttribute("products", products);
      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", reviewPage.getTotalPages());
      model.addAttribute("search", search);
      model.addAttribute("rating", rating);
      model.addAttribute("visible", visible);
      model.addAttribute("productId", productId);

      // Statistics
      model.addAttribute("totalReviews", totalReviews);
      model.addAttribute("visibleReviews", visibleReviews);
      model.addAttribute("hiddenReviews", hiddenReviews);
      model.addAttribute("averageRating", averageRating);

      return "admin/review-management";
   }

   @PostMapping("/toggle-visibility/{id}")
   public String toggleReviewVisibility(@PathVariable Long id, RedirectAttributes redirectAttributes) {
      try {
         Optional<Review> reviewOpt = reviewRepository.findById(id);
         if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            review.setIsVisible(!review.getIsVisible());
            reviewRepository.save(review);

            String action = review.getIsVisible() ? "shown" : "hidden";
            redirectAttributes.addFlashAttribute("success",
                  "Review has been " + action + " successfully!");
         } else {
            redirectAttributes.addFlashAttribute("error", "Review not found!");
         }
      } catch (Exception e) {
         redirectAttributes.addFlashAttribute("error",
               "Error updating review visibility: " + e.getMessage());
      }

      return "redirect:/admin/reviews";
   }

   @PostMapping("/delete/{id}")
   public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
      try {
         if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully!");
         } else {
            redirectAttributes.addFlashAttribute("error", "Review not found!");
         }
      } catch (Exception e) {
         redirectAttributes.addFlashAttribute("error",
               "Error deleting review: " + e.getMessage());
      }

      return "redirect:/admin/reviews";
   }

   @PostMapping("/reply/{id}")
   public String replyToReview(@PathVariable Long id,
                               @RequestParam String replyText,
                               RedirectAttributes redirectAttributes) {
      try {
         reviewService.replyToReview(id, replyText);
         redirectAttributes.addFlashAttribute("success", "Reply added successfully");
      } catch (RuntimeException e) {
         redirectAttributes.addFlashAttribute("error", e.getMessage());
      }
      return "redirect:/admin/reviews";
   }
}
