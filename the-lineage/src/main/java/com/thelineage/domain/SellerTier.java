package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Seller tier. Higher tier = more trust + faster authentication latency.
          TIER_1         - Recently approved. Limited listing volume; subject to extra scrutiny.
          TIER_2         - Established sellers in good standing. Default tier for ongoing activity.
          TIER_3         - High-volume, consistently positive review history.
          VERIFIED_HOUSE - Vetted auction houses, archives, or institutions. Distinct catalog badge.""")
public enum SellerTier {
    TIER_1, TIER_2, TIER_3, VERIFIED_HOUSE
}
