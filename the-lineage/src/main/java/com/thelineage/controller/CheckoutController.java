package com.thelineage.controller;

import com.thelineage.domain.OrderEntity;
import com.thelineage.dto.order.OrderDto;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@Tag(name = "Checkout", description = "Complete purchase from the active cart")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final DomainMappers mappers;

    public CheckoutController(CheckoutService checkoutService, DomainMappers mappers) {
        this.checkoutService = checkoutService;
        this.mappers = mappers;
    }

    @PostMapping
    @Operation(
            summary = "Checkout the active cart",
            description = "Reserves the listing, creates an order, captures payment, marks the listing SOLD, " +
                    "and writes a SOLD provenance record. Returns the resulting order with status PAID."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order created and paid."),
            @ApiResponse(responseCode = "400", description = "Cart is empty or no active cart.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a BUYER.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart references a listing that no longer exists.",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Listing is no longer AVAILABLE.", content = @Content)
    })
    public OrderDto checkout(@AuthenticationPrincipal LineageUserPrincipal principal) {
        OrderEntity order = checkoutService.checkout(principal.id());
        return mappers.toDto(order);
    }
}
