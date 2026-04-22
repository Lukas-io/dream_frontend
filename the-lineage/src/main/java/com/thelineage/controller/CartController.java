package com.thelineage.controller;

import com.thelineage.domain.Cart;
import com.thelineage.domain.CartItem;
import com.thelineage.dto.cart.AddItemRequest;
import com.thelineage.dto.cart.CartDto;
import com.thelineage.dto.cart.CartItemDto;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Buyer cart operations")
public class CartController {

    private final CartService cartService;
    private final DomainMappers mappers;

    public CartController(CartService cartService, DomainMappers mappers) {
        this.cartService = cartService;
        this.mappers = mappers;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated buyer's active cart")
    public CartDto get(@AuthenticationPrincipal LineageUserPrincipal principal) {
        Cart cart = cartService.getActiveCart(principal.id());
        return mappers.toDto(cart);
    }

    @PostMapping("/items")
    @Operation(summary = "Add a listing to the buyer's cart (creates a time-bound reservation)")
    public CartItemDto add(@AuthenticationPrincipal LineageUserPrincipal principal,
                           @Valid @RequestBody AddItemRequest body) {
        CartItem item = cartService.addItem(principal.id(), body.listingId());
        return mappers.toDto(item);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the buyer's cart")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal LineageUserPrincipal principal,
                                       @PathVariable UUID itemId) {
        cartService.removeItem(principal.id(), itemId);
        return ResponseEntity.noContent().build();
    }
}
