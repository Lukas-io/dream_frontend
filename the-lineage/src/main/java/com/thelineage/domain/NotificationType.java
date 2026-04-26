package com.thelineage.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Type of in-app notification (mocked - no external delivery).
          APPLICATION_SUBMITTED - Curators are notified when a new seller application is submitted.
          APPLICATION_UPDATE    - Applicant is notified of approval / rejection.
          LISTING_COMMENT       - Seller is notified of a new comment on their listing.
          ORDER_UPDATE          - Buyer or seller is notified of an order status change.
          PAYOUT                - Seller is notified when escrow is released.
          DISPUTE               - Admin / parties are notified of dispute lifecycle events.""")
public enum NotificationType {
    APPLICATION_UPDATE, LISTING_COMMENT, ORDER_UPDATE, PAYOUT, DISPUTE, APPLICATION_SUBMITTED
}
