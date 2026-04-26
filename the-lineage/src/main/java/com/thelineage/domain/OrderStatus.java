package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Order status timeline:
          CREATED   - Order row exists; payment not yet captured.
          PAID      - Payment captured; escrow HELD; awaiting fulfillment.
          SHIPPED   - Seller dispatched; tracking attached.
          DELIVERED - Carrier reports delivered.
          COMPLETED - Buyer confirmed receipt (or auto-release timeout); escrow RELEASED.
          REFUNDED  - Dispute resolved for buyer; escrow REVERSED.
          DISPUTED  - Buyer raised a dispute; escrow held pending admin resolution.""")
public enum OrderStatus {
    CREATED, PAID, SHIPPED, DELIVERED, COMPLETED, REFUNDED, DISPUTED
}
