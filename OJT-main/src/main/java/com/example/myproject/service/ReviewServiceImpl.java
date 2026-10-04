package com.example.myproject.service;

import com.example.myproject.entity.Review;
import com.example.myproject.entity.Product;
import com.example.myproject.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductAndIsVisible(product, true);
    }

    @Override
    @Transactional
    public void replyToReview(Long reviewId, String replyText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        review.replyToReview(replyText); // sets reply
        reviewRepository.save(review);   // persist the updated review
    }

}