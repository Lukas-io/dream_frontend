package com.thelineage.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Seller marks an order shipped and provides carrier tracking.")
public record ShipRequest(
        @Schema(description = "Carrier name.", example = "UPS")
        @NotBlank String carrier,

        @Schema(description = "Carrier-issued tracking number.", example = "1Z999AA10123456784")
        @NotBlank String trackingNumber
) {}
