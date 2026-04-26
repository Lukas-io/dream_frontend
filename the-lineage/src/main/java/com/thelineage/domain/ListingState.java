package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Listing lifecycle state. Transitions are explicit methods on ListingService:
          AVAILABLE -> RESERVED  (cart add or checkout)
          RESERVED  -> AVAILABLE (cart expiry sweeper)
          RESERVED  -> SOLD      (checkout completes payment)
          AVAILABLE -> UNLISTED  (seller unlists)
          SOLD      -> (terminal, cannot be unlisted)""")
public enum ListingState {
    AVAILABLE, RESERVED, SOLD, UNLISTED
}
