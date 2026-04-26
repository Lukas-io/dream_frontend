package com.thelineage.dto.listing;

import com.thelineage.domain.ListingState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingDto(
        UUID id,
        UUID shoeId,
        String passportId,
        String brand,
        String model,
        String colorway,
        int eraYear,
        BigDecimal price,
        String currency,
        ListingState state,
        Instant createdAt
) {}
