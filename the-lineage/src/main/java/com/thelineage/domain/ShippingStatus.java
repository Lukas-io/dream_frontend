package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Shipping record status (mocked - no real carrier integration).
          PENDING    - Seller has not yet shipped.
          IN_TRANSIT - Carrier has the parcel.
          DELIVERED  - Carrier confirmed delivery.
          LOST       - Carrier reports the parcel lost.""")
public enum ShippingStatus {
    PENDING, IN_TRANSIT, DELIVERED, LOST
}
