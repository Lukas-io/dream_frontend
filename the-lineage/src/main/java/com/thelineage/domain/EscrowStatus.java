package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Escrow state. Independent from PaymentStatus — capture does not auto-release funds.
          HELD     - Captured funds remain with the platform.
          RELEASED - Funds paid out to the seller (after buyer confirms or auto-release timeout).
          REVERSED - Funds returned to the buyer (refund / dispute resolved for buyer).""")
public enum EscrowStatus {
    HELD, RELEASED, REVERSED
}
