package com.thelineage.service;

import com.thelineage.domain.Cart;
import com.thelineage.domain.CartItem;

import java.util.UUID;

public interface CartService {
    Cart getActiveCart(UUID userId);
    CartItem addItem(UUID userId, UUID listingId);
    void removeItem(UUID userId, UUID cartItemId);
    int releaseExpired();
}
