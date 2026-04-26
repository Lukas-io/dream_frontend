package com.thelineage.controller;

import com.thelineage.domain.OrderEntity;
import com.thelineage.domain.UserRole;
import com.thelineage.dto.order.OrderDto;
import com.thelineage.dto.order.ShipRequest;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.OrderService;
import com.thelineage.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order tracking and fulfillment")
public class OrderController {

    private final OrderService orders;
    private final PaymentService payments;
    private final DomainMappers mappers;

    public OrderController(OrderService orders, PaymentService payments, DomainMappers mappers) {
        this.orders = orders;
        this.payments = payments;
        this.mappers = mappers;
    }

    @GetMapping("/me")
    @Operation(summary = "List orders placed by the authenticated buyer")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders for the caller."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content)
    })
    public List<OrderDto> myOrders(@AuthenticationPrincipal LineageUserPrincipal principal) {
        return orders.findByBuyer(principal.id()).stream().map(mappers::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an order (buyer, seller, curator or admin)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the buyer, the seller, or staff.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content)
    })
    public OrderDto get(@AuthenticationPrincipal LineageUserPrincipal principal, @PathVariable UUID id) {
        OrderEntity order = orders.findById(id);
        assertCanSee(principal, order);
        return mappers.toDto(order);
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "Seller marks an order as shipped — creates a ShippingRecord and a SHIPPED provenance entry")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order moved to SHIPPED."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the selling party.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order is not in PAID state.", content = @Content)
    })
    public OrderDto ship(@AuthenticationPrincipal LineageUserPrincipal principal,
                         @PathVariable UUID id,
                         @Valid @RequestBody ShipRequest body) {
        OrderEntity order = orders.findById(id);
        if (!order.getSeller().getUser().getId().equals(principal.id())) {
            throw new ForbiddenException("Only the selling party may mark shipped");
        }
        return mappers.toDto(orders.markShipped(id, body.carrier(), body.trackingNumber()));
    }

    @PostMapping("/{id}/delivered")
    @Operation(summary = "Mark order delivered (seller-confirmed handoff or admin override)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order moved to DELIVERED."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the seller or an admin.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order is not in SHIPPED state.", content = @Content)
    })
    public OrderDto delivered(@AuthenticationPrincipal LineageUserPrincipal principal, @PathVariable UUID id) {
        OrderEntity order = orders.findById(id);
        if (principal.role() != UserRole.ADMIN
                && !order.getSeller().getUser().getId().equals(principal.id())) {
            throw new ForbiddenException("Not permitted");
        }
        return mappers.toDto(orders.markDelivered(id));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Buyer confirms receipt — completes the order and releases escrow")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order COMPLETED, escrow RELEASED, RECEIVED provenance appended."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the buyer.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order cannot be completed from the current state.",
                    content = @Content)
    })
    public OrderDto confirm(@AuthenticationPrincipal LineageUserPrincipal principal, @PathVariable UUID id) {
        OrderEntity order = orders.findById(id);
        if (!order.getBuyer().getId().equals(principal.id())) {
            throw new ForbiddenException("Only the buyer may confirm receipt");
        }
        OrderEntity completed = orders.complete(id);
        payments.findByOrderId(completed.getId()).ifPresent(p -> payments.releaseEscrow(p.getId()));
        return mappers.toDto(completed);
    }

    private void assertCanSee(LineageUserPrincipal principal, OrderEntity order) {
        boolean isBuyer = order.getBuyer().getId().equals(principal.id());
        boolean isSeller = order.getSeller().getUser().getId().equals(principal.id());
        boolean isAdmin = principal.role() == UserRole.ADMIN || principal.role() == UserRole.CURATOR;
        if (!(isBuyer || isSeller || isAdmin)) {
            throw new ForbiddenException("Not permitted to view this order");
        }
    }
}
