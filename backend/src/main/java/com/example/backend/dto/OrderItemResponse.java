package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private String productName;
    private Integer quantity;
    private Double priceAtPurchase;
}