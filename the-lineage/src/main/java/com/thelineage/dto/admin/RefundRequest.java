package com.thelineage.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Admin-initiated refund. Reverses escrow on a HELD payment.")
public record RefundRequest(
        @Schema(description = "Audit reason recorded on the payment.",
                example = "Buyer dispute resolved in their favor.")
        @NotBlank String reason
) {}
