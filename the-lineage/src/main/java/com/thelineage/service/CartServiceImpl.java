package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.CartItemRepository;
import com.thelineage.repository.CartRepository;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ListingRepository listings;
    private final UserRepository users;
    private final Duration reservationTtl;

    public CartServiceImpl(CartRepository carts,
                           CartItemRepository items,
                           ListingRepository listings,
                           UserRepository users,
                           @Value("${lineage.cart.reservation-ttl-minutes:15}") long ttlMinutes) {
        this.carts = carts;
        this.items = items;
        this.listings = listings;
        this.users = users;
        this.reservationTtl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    @Transactional
    public Cart getActiveCart(UUID userId) {
        return carts.findByOwnerId(userId).orElseGet(() -> {
            User owner = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
            return carts.save(Cart.builder().owner(owner).build());
        });
    }

    @Override
    @Transactional
    public CartItem addItem(UUID userId, UUID listingId) {
        Cart cart = getActiveCart(userId);
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found: " + listingId));
        if (listing.getState() != ListingState.AVAILABLE) {
            throw new ConflictException("Listing not available");
        }
        Instant now = Instant.now();
        CartItem item = CartItem.builder()
                .cart(cart)
                .listing(listing)
                .reservedAt(now)
                .expiresAt(now.plus(reservationTtl))
                .build();
        return items.save(item);
    }

    @Override
    @Transactional
    public void removeItem(UUID userId, UUID cartItemId) {
        CartItem item = items.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("CartItem not found: " + cartItemId));
        if (!item.getCart().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Not your cart item");
        }
        items.delete(item);
    }

    @Override
    @Transactional
    public int releaseExpired() {
        List<CartItem> expired = items.findAllByExpiresAtBefore(Instant.now());
        for (CartItem item : expired) {
            Listing listing = item.getListing();
            if (listing.getState() == ListingState.RESERVED) {
                listing.setState(ListingState.AVAILABLE);
                listing.setStateChangedAt(Instant.now());
                listings.save(listing);
            }
            items.delete(item);
        }
        return expired.size();
    }
}
