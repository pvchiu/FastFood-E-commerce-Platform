package com.example.myproject.service;

import com.example.myproject.entity.Review;
import com.example.myproject.entity.Product;
import java.util.List;

public interface ReviewService {
    List<Review> getReviewsByProduct(Product product);
    void replyToReview(Long reviewId, String replyText);

}