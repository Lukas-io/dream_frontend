package com.thelineage.dto.dispute;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Buyer opens a dispute on an order. Order moves to DISPUTED; escrow is held.")
public record OpenDisputeRequest(
        @Schema(description = "ID of the order being disputed.",
                example = "1234abcd-5678-90ef-1234-567890abcdef")
        @NotNull UUID orderId,

        @Schema(description = "Free-text reason. Visible to admins and the seller.",
                example = "Pair arrived with significant midsole damage not shown in the listing photos.")
        @NotBlank @Size(max = 4000) String reason
) {}
