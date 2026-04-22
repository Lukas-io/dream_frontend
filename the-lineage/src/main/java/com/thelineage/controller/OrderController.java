package com.thelineage.controller;

import com.thelineage.domain.OrderEntity;
import com.thelineage.domain.UserRole;
import com.thelineage.dto.order.OrderDto;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.OrderService;
import com.thelineage.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public List<OrderDto> myOrders(@AuthenticationPrincipal LineageUserPrincipal principal) {
        return orders.findByBuyer(principal.id()).stream().map(mappers::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an order (buyer, seller, curator or admin)")
    public OrderDto get(@AuthenticationPrincipal LineageUserPrincipal principal, @PathVariable UUID id) {
        OrderEntity order = orders.findById(id);
        assertCanSee(principal, order);
        return mappers.toDto(order);
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "Seller marks an order as shipped")
    public OrderDto ship(@AuthenticationPrincipal LineageUserPrincipal principal, @PathVariable UUID id) {
        OrderEntity order = orders.findById(id);
        if (!order.getSeller().getUser().getId().equals(principal.id())) {
            throw new ForbiddenException("Only the selling party may mark shipped");
        }
        return mappers.toDto(orders.markShipped(id));
    }

    @PostMapping("/{id}/delivered")
    @Operation(summary = "Mark order delivered (admin override or carrier webhook)")
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
