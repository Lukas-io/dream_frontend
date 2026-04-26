package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Payment processor state. Tracked separately from EscrowStatus.
          INITIATED  - Payment row created; processor not yet contacted.
          AUTHORIZED - Funds reserved on the buyer's instrument but not yet captured.
          CAPTURED   - Funds taken from the buyer; held in escrow until released.
          FAILED     - Processor declined.
          REFUNDED   - Funds returned to the buyer (paired with EscrowStatus REVERSED).""")
public enum PaymentStatus {
    INITIATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED
}
