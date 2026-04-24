package com.thelineage.controller;

import com.thelineage.domain.Dispute;
import com.thelineage.dto.dispute.DisputeDto;
import com.thelineage.dto.dispute.OpenDisputeRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Disputes", description = "Order dispute workflow")
public class DisputeController {

    private final DisputeService disputes;
    private final DomainMappers mappers;

    public DisputeController(DisputeService disputes, DomainMappers mappers) {
        this.disputes = disputes;
        this.mappers = mappers;
    }

    @PostMapping("/disputes")
    @Operation(
            summary = "Buyer opens a dispute on an order",
            description = "Moves the order to DISPUTED, holds escrow, and appends a DISPUTED provenance record."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dispute opened."),
            @ApiResponse(responseCode = "400", description = "Validation failed or reason blank.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Caller is not a BUYER or not the buyer on the order.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order is already COMPLETED or REFUNDED.",
                    content = @Content)
    })
    public ResponseEntity<DisputeDto> open(@AuthenticationPrincipal LineageUserPrincipal principal,
                                           @Valid @RequestBody OpenDisputeRequest body) {
        Dispute d = disputes.open(body.orderId(), principal.id(), body.reason());
        return ResponseEntity.created(URI.create("/disputes/" + d.getId())).body(mappers.toDto(d));
    }

    @GetMapping("/orders/{orderId}/disputes")
    @Operation(summary = "List disputes for an order")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disputes for the order."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content)
    })
    public List<DisputeDto> byOrder(@PathVariable UUID orderId) {
        return disputes.findByOrder(orderId).stream().map(mappers::toDto).toList();
    }
}
