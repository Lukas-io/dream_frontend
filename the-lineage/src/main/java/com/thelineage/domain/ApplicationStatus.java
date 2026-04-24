package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Seller application status (also cached on SellerProfile for fast gating).
          NONE     - No application submitted.
          PENDING  - Submitted; awaiting curator review.
          APPROVED - Approved; user is a SELLER and may submit shoes / list.
          REJECTED - Curator declined; user may reapply.""")
public enum ApplicationStatus {
    NONE, PENDING, APPROVED, REJECTED
}
