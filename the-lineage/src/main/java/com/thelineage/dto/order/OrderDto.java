package com.thelineage.dto.order;

import com.thelineage.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID listingId,
        UUID buyerId,
        UUID sellerProfileId,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        Instant createdAt,
        Instant completedAt
) {}
