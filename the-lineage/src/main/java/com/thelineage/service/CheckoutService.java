package com.thelineage.service;

import com.thelineage.domain.OrderEntity;

import java.util.UUID;

public interface CheckoutService {
    OrderEntity checkout(UUID buyerUserId);
}
