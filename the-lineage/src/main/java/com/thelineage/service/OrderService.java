package com.thelineage.service;

import com.thelineage.domain.OrderEntity;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderEntity createFromCart(UUID buyerUserId);
    OrderEntity findById(UUID orderId);
    List<OrderEntity> findByBuyer(UUID buyerUserId);
    OrderEntity markShipped(UUID orderId, String carrier, String trackingNumber);
    OrderEntity markDelivered(UUID orderId);
    OrderEntity complete(UUID orderId);
}
