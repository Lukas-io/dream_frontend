package com.thelineage.dto.cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CartItemDto(
        UUID id,
        UUID listingId,
        BigDecimal price,
        String currency,
        Instant reservedAt,
        Instant expiresAt
) {}
