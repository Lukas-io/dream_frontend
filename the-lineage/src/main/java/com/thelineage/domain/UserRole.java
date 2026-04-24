package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Account role. Capability inheritance: ADMIN > CURATOR > BUYER, and SELLER > BUYER.
          BUYER   - default for new accounts; can browse, comment, purchase.
          SELLER  - approved seller; can submit shoes and create listings.
          CURATOR - reviews seller applications; authenticates shoes.
          ADMIN   - resolves disputes, manages payouts, can override curator decisions.""")
public enum UserRole {
    BUYER, SELLER, CURATOR, ADMIN
}
