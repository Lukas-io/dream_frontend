package com.thelineage.dto.seller;

import com.thelineage.domain.SellerTier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApproveApplicationRequest(
        @NotNull SellerTier tier,
        @Size(max = 2000) String note
) {}
