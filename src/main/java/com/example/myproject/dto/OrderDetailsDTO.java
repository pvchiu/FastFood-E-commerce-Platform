package com.example.myproject.dto;

import com.example.myproject.entity.OrderItem.OrderStatus;
import com.example.myproject.entity.OrderItem.PaymentMethod;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDTO {
   private Long id;
   private String orderNumber;
   private BigDecimal totalAmount;
   private OrderStatus status;
   private String deliveryAddress;
   private String notes;
   private PaymentMethod paymentMethod;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
   private UserSummaryDTO user;
   private List<OrderItemDetailsDTO> orderItems;

   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class UserSummaryDTO {
      private Long id;
      private String username;
      private String email;
      private String fullName;
      private String phoneNumber;
   }

   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class OrderItemDetailsDTO {
      private Long id;
      private Integer quantity;
      private BigDecimal unitPrice;
      private BigDecimal subtotal;
      private String specialInstructions;
      private ProductSummaryDTO product;
   }

   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public static class ProductSummaryDTO {
      private Long id;
      private String name;
      private String description;
      private String imageUrl;
   }
}
