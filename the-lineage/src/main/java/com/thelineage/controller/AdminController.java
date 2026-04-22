package com.thelineage.controller;

import com.thelineage.domain.Dispute;
import com.thelineage.domain.Payment;
import com.thelineage.domain.User;
import com.thelineage.dto.admin.RefundRequest;
import com.thelineage.dto.admin.UpdateRoleRequest;
import com.thelineage.dto.dispute.DisputeDto;
import com.thelineage.dto.dispute.ResolveDisputeRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.DisputeService;
import com.thelineage.service.PaymentService;
import com.thelineage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Platform administration")
public class AdminController {

    private final DisputeService disputes;
    private final PaymentService payments;
    private final UserService users;
    private final DomainMappers mappers;

    public AdminController(DisputeService disputes, PaymentService payments, UserService users, DomainMappers mappers) {
        this.disputes = disputes;
        this.payments = payments;
        this.users = users;
        this.mappers = mappers;
    }

    public record PaymentDto(UUID id, UUID orderId, String processorReference, String paymentStatus,
                             String escrowStatus, java.math.BigDecimal amount, Instant capturedAt,
                             Instant escrowReleasedAt) {
        public static PaymentDto from(Payment p) {
            return new PaymentDto(p.getId(), p.getOrder().getId(), p.getProcessorReference(),
                    p.getPaymentStatus().name(), p.getEscrowStatus().name(), p.getAmount(),
                    p.getCapturedAt(), p.getEscrowReleasedAt());
        }
    }

    @PostMapping("/disputes/{id}/resolve")
    @Operation(summary = "Resolve a dispute for the buyer or the seller")
    public DisputeDto resolve(@AuthenticationPrincipal LineageUserPrincipal principal,
                              @PathVariable UUID id,
                              @Valid @RequestBody ResolveDisputeRequest body) {
        Dispute d = disputes.resolve(id, principal.id(), body.outcome(), body.note());
        return mappers.toDto(d);
    }

    @PostMapping("/payments/{id}/release-escrow")
    @Operation(summary = "Manually release escrow for a captured payment")
    public PaymentDto releaseEscrow(@PathVariable UUID id) {
        return PaymentDto.from(payments.releaseEscrow(id));
    }

    @PostMapping("/payments/{id}/refund")
    @Operation(summary = "Refund a payment (reverses escrow)")
    public PaymentDto refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest body) {
        return PaymentDto.from(payments.refund(id, body.reason()));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Override a user's role")
    public String updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest body) {
        User user = users.updateRole(id, body.role());
        return user.getRole().name();
    }
}
