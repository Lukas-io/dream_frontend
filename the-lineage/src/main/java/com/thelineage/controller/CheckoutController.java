package com.thelineage.controller;

import com.thelineage.domain.OrderEntity;
import com.thelineage.dto.order.OrderDto;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Checkout the active cart — reserves, creates order, captures payment, marks sold")
    public OrderDto checkout(@AuthenticationPrincipal LineageUserPrincipal principal) {
        OrderEntity order = checkoutService.checkout(principal.id());
        return mappers.toDto(order);
    }
}
