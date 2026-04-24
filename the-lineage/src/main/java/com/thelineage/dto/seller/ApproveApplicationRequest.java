package com.thelineage.dto.seller;

import com.thelineage.domain.SellerTier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Curator approves a pending seller application and assigns an initial tier.")
public record ApproveApplicationRequest(
        @Schema(description = "Tier to grant the seller. New approvals usually start at TIER_1.",
                example = "TIER_1")
        @NotNull SellerTier tier,

        @Schema(description = "Optional reviewer note shown to the applicant in the approval notification.",
                example = "Strong references; starting at TIER_1 with a 30-day re-review.")
        @Size(max = 2000) String note
) {}
