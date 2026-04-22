package com.thelineage.controller;

import com.thelineage.domain.Dispute;
import com.thelineage.dto.dispute.DisputeDto;
import com.thelineage.dto.dispute.OpenDisputeRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Buyer opens a dispute on an order")
    public ResponseEntity<DisputeDto> open(@AuthenticationPrincipal LineageUserPrincipal principal,
                                           @Valid @RequestBody OpenDisputeRequest body) {
        Dispute d = disputes.open(body.orderId(), principal.id(), body.reason());
        return ResponseEntity.created(URI.create("/disputes/" + d.getId())).body(mappers.toDto(d));
    }

    @GetMapping("/orders/{orderId}/disputes")
    @Operation(summary = "List disputes for an order")
    public List<DisputeDto> byOrder(@PathVariable UUID orderId) {
        return disputes.findByOrder(orderId).stream().map(mappers::toDto).toList();
    }
}
