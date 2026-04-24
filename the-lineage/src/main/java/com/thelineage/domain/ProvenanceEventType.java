package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Provenance ledger event type. Records are append-only and never edited.
          SUBMITTED     - Seller submitted a shoe for authentication.
          AUTHENTICATED - Curator confirmed genuineness, set grade and rarity.
          LISTED        - Seller created a listing for the shoe.
          SOLD          - Listing transitioned to SOLD via checkout.
          SHIPPED       - Seller dispatched the shoe.
          RECEIVED      - Buyer confirmed receipt.
          DISPUTED      - Buyer raised a dispute.
          RESOLVED      - Admin resolved a dispute (payload notes the outcome).
          UNLISTED      - Seller unlisted the listing.""")
public enum ProvenanceEventType {
    SUBMITTED, AUTHENTICATED, LISTED, SOLD, SHIPPED, RECEIVED, DISPUTED, RESOLVED, UNLISTED
}
