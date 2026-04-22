package com.thelineage.dto.order;

import jakarta.validation.constraints.NotBlank;

public record ShipRequest(@NotBlank String carrier, @NotBlank String trackingNumber) {}
