package com.example.myproject.service;

import com.example.myproject.dto.ProductDTO;
import com.example.myproject.entity.Category;
import com.example.myproject.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.myproject.entity.Product;
import com.example.myproject.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByIsAvailableTrue();
    }

    // Updated method with sorting and filtering
    public List<Product> getFilteredAndSortedProducts(List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByCategoryIdInAndPriceBetweenAndIsAvailableTrue(
                        categoryIds, minPrice, maxPrice, sort);
            } else if (minPrice != null) {
                return productRepository.findByCategoryIdInAndPriceGreaterThanEqualAndIsAvailableTrue(
                        categoryIds, minPrice, sort);
            } else if (maxPrice != null) {
                return productRepository.findByCategoryIdInAndPriceLessThanEqualAndIsAvailableTrue(
                        categoryIds, maxPrice, sort);
            } else {
                return productRepository.findByCategoryIdInAndIsAvailableTrue(categoryIds, sort);
            }
        } else {
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByPriceBetweenAndIsAvailableTrue(minPrice, maxPrice, sort);
            } else if (minPrice != null) {
                return productRepository.findByPriceGreaterThanEqualAndIsAvailableTrue(minPrice, sort);
            } else if (maxPrice != null) {
                return productRepository.findByPriceLessThanEqualAndIsAvailableTrue(maxPrice, sort);
            } else {
                return productRepository.findByIsAvailableTrue(sort);
            }
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        switch (sortBy != null ? sortBy.toLowerCase() : "default") {
            case "price":
                return Sort.by(direction, "price");
            case "name":
                return Sort.by(direction, "name");
            case "created":
                return Sort.by(direction, "createdAt");
            default:
                return Sort.by(Sort.Direction.ASC, "name");
        }
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findByIsAvailableTrue(); 
        }
        return productRepository.findByKeyword(keyword.trim());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setIsAvailable(productDTO.getIsAvailable());
        product.setStockQuantity(productDTO.getStockQuantity());
        if (productDTO.getCategory() != null && productDTO.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Category not found with id: " + productDTO.getCategory().getId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // Update only non-null fields from productDetails
                    if (productDetails.getName() != null) {
                        existingProduct.setName(productDetails.getName());
                    }

                    if (productDetails.getDescription() != null) {
                        existingProduct.setDescription(productDetails.getDescription());
                    }

                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }

                    if (productDetails.getImageUrl() != null) {
                        existingProduct.setImageUrl(productDetails.getImageUrl());
                    }

                    if (productDetails.getStockQuantity() != null) {
                        existingProduct.setStockQuantity(productDetails.getStockQuantity());
                    }

                    if (productDetails.getIsAvailable() != null) {
                        existingProduct.setIsAvailable(productDetails.getIsAvailable());
                    }

                    // Handle category update properly
                    if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
                        Category category = categoryRepository.findById(productDetails.getCategory().getId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Category not found with id: " + productDetails.getCategory().getId()));
                        existingProduct.setCategory(category);
                    }

                    existingProduct.setUpdatedAt(LocalDateTime.now());

                    Product updatedProduct = productRepository.save(existingProduct);
                    return ProductDTO.fromEntity(updatedProduct);
                })
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    public Page<Product> getAllAvailableProducts(Pageable pageable) {
        return productRepository.findByIsAvailableTrue(pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }

    public Page<Product> getFilteredAndSortedProducts(List<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByCategoryIdInAndPriceBetweenAndIsAvailableTrue(categoryIds, minPrice,
                        maxPrice, pageable);
            } else if (minPrice != null) {
                return productRepository.findByCategoryIdInAndPriceGreaterThanEqualAndIsAvailableTrue(categoryIds,
                        minPrice, pageable);
            } else if (maxPrice != null) {
                return productRepository.findByCategoryIdInAndPriceLessThanEqualAndIsAvailableTrue(categoryIds,
                        maxPrice, pageable);
            } else {
                return productRepository.findByCategoryIdInAndIsAvailableTrue(categoryIds, pageable);
            }
        } else {
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByPriceBetweenAndIsAvailableTrue(minPrice, maxPrice, pageable);
            } else if (minPrice != null) {
                return productRepository.findByPriceGreaterThanEqualAndIsAvailableTrue(minPrice, pageable);
            } else if (maxPrice != null) {
                return productRepository.findByPriceLessThanEqualAndIsAvailableTrue(maxPrice, pageable);
            } else {
                return productRepository.findByIsAvailableTrue(pageable);
            }
        }
    }

}