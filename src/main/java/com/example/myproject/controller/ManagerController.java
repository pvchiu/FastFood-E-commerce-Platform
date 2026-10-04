package com.example.myproject.controller;

import com.example.myproject.dto.OrderDTO;
import com.example.myproject.dto.OrderDetailsDTO;
import com.example.myproject.dto.ProductDTO;
import com.example.myproject.dto.UpdateOrderStatusDTO;
import com.example.myproject.entity.Category;
import com.example.myproject.entity.Order;
import com.example.myproject.entity.User;
import com.example.myproject.repository.ProductRepository;
import com.example.myproject.service.CategoryService;
import com.example.myproject.service.OrderService;
import com.example.myproject.service.ProductService;
import com.example.myproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    // Product manager
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping("/products/add")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.createProduct(productDTO));
    }

    @PutMapping("/products/update/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDetails) {

        ProductDTO updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/products/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        try {
            if (!productRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "Product not found"));
            }

            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "Error deleting product: " + e.getMessage()));
        }
    }

    // User manager
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users/add")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Order manager
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrderSummaries());
    }

    @GetMapping("/orders/search")
    public ResponseEntity<List<OrderDTO>> searchOrders(@RequestParam(required = false) String q) {
        List<Order> orders = orderService.searchOrders(q);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setId(order.getId());
                    dto.setOrderNumber(order.getOrderNumber());
                    dto.setTotalAmount(order.getTotalAmount());
                    dto.setStatus(order.getStatus().name());
                    dto.setPaymentMethod(order.getPaymentMethod().name());
                    dto.setCreatedAt(order.getCreatedAt());

                    if (order.getUser() != null) {
                        dto.setUserId(order.getUser().getId());
                        dto.setUsername(order.getUser().getUsername());
                        dto.setEmail(order.getUser().getEmail());
                        dto.setFullName(order.getUser().getFullName());
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(orderDTOs);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusDTO updateDto) {

        Order updatedOrder = orderService.updateOrderStatus(orderId, updateDto);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long orderId) {
        OrderDetailsDTO orderDetails = orderService.getOrderDetailsDTO(orderId);
        return ResponseEntity.ok(orderDetails);
    }


}
