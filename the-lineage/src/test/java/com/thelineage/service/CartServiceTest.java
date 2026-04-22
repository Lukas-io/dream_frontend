package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.repository.CartItemRepository;
import com.thelineage.repository.CartRepository;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository carts;
    @Mock private CartItemRepository items;
    @Mock private ListingRepository listings;
    @Mock private UserRepository users;
    private CartServiceImpl service;

    @BeforeEach
    void setup() {
        service = new CartServiceImpl(carts, items, listings, users, 15);
    }

    @Test
    void addItem_createsCartIfMissingAndPersistsItem() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        User u = User.builder().id(userId).build();
        when(carts.findByOwnerId(userId)).thenReturn(Optional.empty());
        when(users.findById(userId)).thenReturn(Optional.of(u));
        Cart cart = Cart.builder().id(UUID.randomUUID()).owner(u).build();
        when(carts.save(any())).thenReturn(cart);
        Listing listing = Listing.builder().id(listingId).state(ListingState.AVAILABLE).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(items.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CartItem item = service.addItem(userId, listingId);
        assertThat(item.getListing()).isSameAs(listing);
        assertThat(item.getCart()).isSameAs(cart);
    }

    @Test
    void addItem_whenListingNotAvailable_throwsConflict() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        User u = User.builder().id(userId).build();
        when(carts.findByOwnerId(userId)).thenReturn(Optional.of(Cart.builder().owner(u).build()));
        when(listings.findById(listingId)).thenReturn(Optional.of(Listing.builder().state(ListingState.SOLD).build()));
        assertThatThrownBy(() -> service.addItem(userId, listingId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void removeItem_whenNotOwner_throwsForbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        CartItem item = CartItem.builder().id(itemId)
                .cart(Cart.builder().owner(User.builder().id(otherId).build()).build()).build();
        when(items.findById(itemId)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> service.removeItem(userId, itemId)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void reserveListing_whenAvailable_marksReservedAndCreatesItem() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        User u = User.builder().id(userId).build();
        Cart cart = Cart.builder().owner(u).build();
        when(carts.findByOwnerId(userId)).thenReturn(Optional.of(cart));
        Listing listing = Listing.builder().id(listingId).state(ListingState.AVAILABLE).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(listings.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(items.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CartItem item = service.reserveListing(userId, listingId);
        assertThat(listing.getState()).isEqualTo(ListingState.RESERVED);
        assertThat(item.getListing()).isSameAs(listing);
    }

    @Test
    void releaseExpired_flipsReservedListingsBackToAvailableAndDeletesItems() {
        Listing reserved = Listing.builder().state(ListingState.RESERVED).build();
        CartItem expired = CartItem.builder().id(UUID.randomUUID())
                .expiresAt(Instant.now().minusSeconds(60)).listing(reserved).build();
        when(items.findAllByExpiresAtBefore(any())).thenReturn(List.of(expired));
        int released = service.releaseExpired();
        assertThat(released).isEqualTo(1);
        assertThat(reserved.getState()).isEqualTo(ListingState.AVAILABLE);
        verify(items).delete(expired);
    }
}
