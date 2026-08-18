package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.exception.InsufficientStockException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Order;
import com.example.backend.model.OrderItem;
import com.example.backend.model.Product;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        double total = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '" + product.getName() +
                                "'. Available: " + product.getStockQuantity() +
                                ", Requested: " + itemRequest.getQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setPriceAtPurchase(product.getPrice());
            item.setOrder(order);

            order.getItems().add(item);

            total += product.getPrice() * itemRequest.getQuantity();
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderItemResponse ir = new OrderItemResponse();
                    ir.setProductName(item.getProduct().getName());
                    ir.setQuantity(item.getQuantity());
                    ir.setPriceAtPurchase(item.getPriceAtPurchase());
                    return ir;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}