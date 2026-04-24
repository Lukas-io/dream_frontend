package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Dispute lifecycle.
          OPEN            - Newly raised; awaiting admin resolution.
          RESOLVED_BUYER  - Resolved in favor of the buyer; payment refunded.
          RESOLVED_SELLER - Resolved in favor of the seller; escrow released.""")
public enum DisputeStatus {
    OPEN, RESOLVED_BUYER, RESOLVED_SELLER
}
