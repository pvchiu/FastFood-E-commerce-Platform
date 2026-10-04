package com.example.myproject.dto;

import com.example.myproject.entity.Product;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer stockQuantity;
    private CategoryDTO category; // Nested DTO

    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsAvailable(product.getIsAvailable());
        dto.setStockQuantity(product.getStockQuantity());

        if(product.getCategory() != null) {
            dto.setCategory(CategoryDTO.fromEntity(product.getCategory()));
        }

        return dto;
    }
}
