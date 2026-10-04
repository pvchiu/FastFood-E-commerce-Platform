package com.example.myproject.repository;

import com.example.myproject.entity.Review;
import com.example.myproject.entity.Product;
import com.example.myproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
        List<Review> findByProduct(Product product);

        List<Review> findByProductAndIsVisible(Product product, Boolean isVisible);

        boolean existsByUserAndProduct(User user, Product product);

        List<Review> findByUser(User user);

        // Basic pagination methods
        Page<Review> findByRating(Integer rating, Pageable pageable);

        Page<Review> findByIsVisible(Boolean visible, Pageable pageable);

        Page<Review> findByProductId(Long productId, Pageable pageable);

        Page<Review> findByRatingAndIsVisible(Integer rating, Boolean visible, Pageable pageable);

        Page<Review> findByRatingAndIsVisibleAndProductId(Integer rating, Boolean visible, Long productId,
                        Pageable pageable);

        // Count methods for statistics
        long countByIsVisible(Boolean visible);

        // Custom search query that searches in comments, user full name, and username
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Review> findBySearchTerm(@Param("search") String search, Pageable pageable);

        // Custom search with rating filter
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "r.rating = :rating")
        Page<Review> findBySearchTermAndRating(@Param("search") String search, @Param("rating") Integer rating,
                        Pageable pageable);

        // Custom search with visibility filter
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "r.isVisible = :visible")
        Page<Review> findBySearchTermAndIsVisible(@Param("search") String search, @Param("visible") Boolean visible,
                        Pageable pageable);

        // Custom search with rating and visibility filters
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "r.rating = :rating AND r.isVisible = :visible")
        Page<Review> findBySearchTermAndRatingAndIsVisible(@Param("search") String search,
                        @Param("rating") Integer rating,
                        @Param("visible") Boolean visible, Pageable pageable);

        // Custom search with product filter
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "r.product.id = :productId")
        Page<Review> findBySearchTermAndProductId(@Param("search") String search, @Param("productId") Long productId,
                        Pageable pageable);

        // Custom search with all filters
        @Query("SELECT r FROM Review r WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "r.rating = :rating AND r.isVisible = :visible AND r.product.id = :productId")
        Page<Review> findBySearchTermAndRatingAndIsVisibleAndProductId(@Param("search") String search,
                        @Param("rating") Integer rating, @Param("visible") Boolean visible,
                        @Param("productId") Long productId,
                        Pageable pageable);

        Optional<Review> findByUserAndProduct(User user, Product product);

        @Query("SELECT r.product.id FROM Review r WHERE r.user.id = :userId")
        List<Long> findReviewedProductIdsByUserId(@Param("userId") Long userId);

}