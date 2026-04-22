package com.thelineage.dto.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID orderId,
        UUID authorUserId,
        UUID sellerProfileId,
        int accuracyScore,
        int conditionScore,
        int shippingScore,
        String body,
        Instant createdAt
) {}
