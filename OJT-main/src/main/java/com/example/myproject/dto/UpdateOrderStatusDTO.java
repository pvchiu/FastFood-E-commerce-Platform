package com.example.myproject.dto;

import com.example.myproject.entity.OrderItem.OrderStatus;

public class UpdateOrderStatusDTO {
    private OrderStatus status;
    private String reason;

    // Getters and setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
