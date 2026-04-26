package com.thelineage.dto.dispute;

import com.thelineage.domain.DisputeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin's verdict on an open dispute. Drives a refund or escrow release.")
public record ResolveDisputeRequest(
        @Schema(description = "Resolution outcome. Must be RESOLVED_BUYER (refund) or RESOLVED_SELLER (release escrow).",
                example = "RESOLVED_BUYER")
        @NotNull DisputeStatus outcome,

        @Schema(description = "Optional admin note recorded with the resolution.",
                example = "Buyer provided photo evidence; refunding.")
        @Size(max = 4000) String note
) {}
