package com.example.myproject.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.myproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Basic filters
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByIsAvailableTrue();
    List<Product> findByIsAvailableTrue(Sort sort);
    
    // Category filters with sorting
    List<Product> findByCategoryIdInAndIsAvailableTrue(List<Long> categoryIds, Sort sort);
    
    // Price filters with sorting
    List<Product> findByPriceBetweenAndIsAvailableTrue(BigDecimal minPrice, BigDecimal maxPrice, Sort sort);
    List<Product> findByPriceGreaterThanEqualAndIsAvailableTrue(BigDecimal minPrice, Sort sort);
    List<Product> findByPriceLessThanEqualAndIsAvailableTrue(BigDecimal maxPrice, Sort sort);
    
    // Combined category and price filters with sorting
    List<Product> findByCategoryIdInAndPriceBetweenAndIsAvailableTrue(
        List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Sort sort);
    List<Product> findByCategoryIdInAndPriceGreaterThanEqualAndIsAvailableTrue(
        List<Long> categoryIds, BigDecimal minPrice, Sort sort);
    List<Product> findByCategoryIdInAndPriceLessThanEqualAndIsAvailableTrue(
        List<Long> categoryIds, BigDecimal maxPrice, Sort sort);
    
    // Search with keyword
    @Query("SELECT p FROM Product p WHERE (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.isAvailable = true")
    List<Product> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isAvailable = true")
    List<Product> findAvailableProducts();

    @Query("SELECT p FROM Product p WHERE (p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.isAvailable = true")
Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

Page<Product> findByCategoryIdInAndPriceBetweenAndIsAvailableTrue(List<Long> ids, BigDecimal min, BigDecimal max, Pageable pageable);
Page<Product> findByCategoryIdInAndPriceGreaterThanEqualAndIsAvailableTrue(List<Long> ids, BigDecimal min, Pageable pageable);
Page<Product> findByCategoryIdInAndPriceLessThanEqualAndIsAvailableTrue(List<Long> ids, BigDecimal max, Pageable pageable);
Page<Product> findByCategoryIdInAndIsAvailableTrue(List<Long> ids, Pageable pageable);

Page<Product> findByPriceBetweenAndIsAvailableTrue(BigDecimal min, BigDecimal max, Pageable pageable);
Page<Product> findByPriceGreaterThanEqualAndIsAvailableTrue(BigDecimal min, Pageable pageable);
Page<Product> findByPriceLessThanEqualAndIsAvailableTrue(BigDecimal max, Pageable pageable);
Page<Product> findByIsAvailableTrue(Pageable pageable);


}