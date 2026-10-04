package com.example.myproject.service;

import com.example.myproject.dto.OrderDTO;
import com.example.myproject.dto.OrderDetailsDTO;
import com.example.myproject.dto.UpdateOrderStatusDTO;
import com.example.myproject.entity.CartItem;
import com.example.myproject.entity.Order;
import com.example.myproject.entity.OrderItem;
import com.example.myproject.entity.OrderItem.OrderStatus;
import com.example.myproject.entity.User;
import com.example.myproject.repository.CartItemRepository;
import com.example.myproject.repository.OrderItemRepository;
import com.example.myproject.repository.OrderRepository;
import com.example.myproject.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;

    private final OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;


    OrderService(OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository) {
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findByStatus(OrderItem.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void createOrder(String username, String paymentMethod) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderItem.OrderStatus.PENDING);
        order.setPaymentMethod(OrderItem.PaymentMethod.valueOf(paymentMethod));

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem
                    .setSubtotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemRepository.save(orderItem);
        }

        cartItemRepository.deleteAll(cartItems);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order findByIdWithDetails(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // Force load orderItems and products to avoid lazy loading issues
        order.getOrderItems().forEach(item -> {
            if (item.getProduct() != null) {
                // Force load product details
                item.getProduct().getName();
            }
        });
        return order;
    }

    public BigDecimal getTotalSpentByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, UpdateOrderStatusDTO updateDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = updateDto.getStatus();

        // Validate status transition - CANCELLED should be final
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a cancelled order");
        }

        // COMPLETED should be final status
        if (currentStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change status from COMPLETED");
        }

        // Set new status
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Send email if necessary
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            String to = order.getUser().getEmail();
            String subject;
            String body;

            // Get order details
            OrderDetailsDTO orderDetails = getOrderDetailsDTO(orderId);
            StringBuilder orderItemsDetails = new StringBuilder();

            // Build order items details
            for (OrderDetailsDTO.OrderItemDetailsDTO item : orderDetails.getOrderItems()) {
                orderItemsDetails.append(String.format(
                        "- %s x%d (Unit Price: $%.2f, Subtotal: $%.2f)\n",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ));
            }

            String orderSummary = String.format(
                    "\nOrder Details:" +
                            "\nOrder Number: %s" +
                            "\nTotal Amount: $%.2f" +
                            "\nPayment Method: %s" +
                            "\nOrder Date: %s" +
                            "\n\nItems:\n%s",
                    orderDetails.getOrderNumber(),
                    orderDetails.getTotalAmount(),
                    orderDetails.getPaymentMethod(),
                    orderDetails.getCreatedAt().toString(),
                    orderItemsDetails
            );

            if (newStatus == OrderStatus.COMPLETED) {
                subject = "Order Completed Successfully";
                body = "Dear " + order.getUser().getFullName() + ",\n\n" +
                        "We're happy to inform you that your order has been completed.\n"
                        +orderSummary + "\n\n" +
                        "Thank you for shopping with us!\n\n" +
                        "Best regards,\nYour Store Team";
                emailService.sendEmail(to, subject, body);
            } else if (newStatus == OrderStatus.CANCELLED) {
                String reason = updateDto.getReason(); // Add 'reason' in your DTO
                subject = "Order Cancelled";
                body = "Dear " + order.getUser().getFullName() + ",\n\n" +
                        "Unfortunately, your order has been cancelled.\n" +
                        "Reason: " + (reason != null ? reason : "No reason provided") + "\n\n" +
                        orderSummary + "\n\n" +
                        "If you have any questions, please contact support.\n\n" +
                        "Sincerely,\nYour Store Team";
                emailService.sendEmail(to, subject, body);
            }
        }

        return updatedOrder;
    }


    public List<Order> findByUserIdWithItems(Long userId) {
        return orderRepository.findByUserIdWithItems(userId);
    }

    public List<OrderItem> findOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public OrderDetailsDTO getOrderDetailsDTO(Long orderId) {
        Order order = findByIdWithDetails(orderId);
        return convertToOrderDetailsDTO(order);
    }

    public List<Order> searchOrders(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllOrders();
        }
        return orderRepository.searchOrders(searchTerm.trim());
    }

    private OrderDetailsDTO convertToOrderDetailsDTO(Order order) {
        OrderDetailsDTO dto = new OrderDetailsDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setNotes(order.getNotes());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert user
        if (order.getUser() != null) {
            OrderDetailsDTO.UserSummaryDTO userDto = new OrderDetailsDTO.UserSummaryDTO();
            userDto.setId(order.getUser().getId());
            userDto.setUsername(order.getUser().getUsername());
            userDto.setEmail(order.getUser().getEmail());
            userDto.setFullName(order.getUser().getFullName());
            userDto.setPhoneNumber(order.getUser().getPhoneNumber());
            dto.setUser(userDto);
        }

        // Convert order items
        List<OrderDetailsDTO.OrderItemDetailsDTO> itemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDetailsDTO)
                .toList();
        dto.setOrderItems(itemDtos);

        return dto;
    }

    private OrderDetailsDTO.OrderItemDetailsDTO convertToOrderItemDetailsDTO(OrderItem item) {
        OrderDetailsDTO.OrderItemDetailsDTO dto = new OrderDetailsDTO.OrderItemDetailsDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setSpecialInstructions(item.getSpecialInstructions());

        // Convert product
        if (item.getProduct() != null) {
            OrderDetailsDTO.ProductSummaryDTO productDto = new OrderDetailsDTO.ProductSummaryDTO();
            productDto.setId(item.getProduct().getId());
            productDto.setName(item.getProduct().getName());
            productDto.setDescription(item.getProduct().getDescription());
            productDto.setImageUrl(item.getProduct().getImageUrl());
            dto.setProduct(productDto);
        }

        return dto;
    }

    public List<OrderDTO> getAllOrderSummaries() {
        List<Order> orders = orderRepository.findAllWithUser(); // We'll write this query next
        return orders.stream().map(order -> {
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
        }).toList();
    }

}
