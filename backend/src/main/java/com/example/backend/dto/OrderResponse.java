package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String customerName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<OrderItemResponse> items;
}