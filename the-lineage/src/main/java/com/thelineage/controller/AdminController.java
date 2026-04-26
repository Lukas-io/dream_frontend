package com.thelineage.controller;

import com.thelineage.domain.Dispute;
import com.thelineage.domain.DisputeStatus;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(
            summary = "Resolve a dispute for the buyer or the seller",
            description = "Updates the dispute and order, then automatically refunds the payment " +
                    "(RESOLVED_BUYER) or releases escrow to the seller (RESOLVED_SELLER). " +
                    "Appends a RESOLVED provenance record."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispute resolved; payment side-effect applied."),
            @ApiResponse(responseCode = "400", description = "Outcome must be RESOLVED_BUYER or RESOLVED_SELLER.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispute or admin user not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Dispute already resolved.", content = @Content)
    })
    public DisputeDto resolve(@AuthenticationPrincipal LineageUserPrincipal principal,
                              @PathVariable UUID id,
                              @Valid @RequestBody ResolveDisputeRequest body) {
        Dispute d = disputes.resolve(id, principal.id(), body.outcome(), body.note());
        UUID orderId = d.getOrder().getId();
        payments.findByOrderId(orderId).ifPresent(p -> {
            if (body.outcome() == DisputeStatus.RESOLVED_BUYER) {
                payments.refund(p.getId(), body.note() != null ? body.note() : "Dispute resolved for buyer");
            } else if (body.outcome() == DisputeStatus.RESOLVED_SELLER) {
                payments.releaseEscrow(p.getId());
            }
        });
        return mappers.toDto(d);
    }

    @PostMapping("/payments/{id}/release-escrow")
    @Operation(summary = "Manually release escrow for a captured payment")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escrow released."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Payment not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Payment is not CAPTURED+HELD.", content = @Content)
    })
    public PaymentDto releaseEscrow(@PathVariable UUID id) {
        return PaymentDto.from(payments.releaseEscrow(id));
    }

    @PostMapping("/payments/{id}/refund")
    @Operation(summary = "Refund a payment (reverses escrow)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded; escrow reversed."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Payment not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Escrow is not currently HELD.", content = @Content)
    })
    public PaymentDto refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest body) {
        return PaymentDto.from(payments.refund(id, body.reason()));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Override a user's role")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated; returns the new role name."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    })
    public String updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest body) {
        User user = users.updateRole(id, body.role());
        return user.getRole().name();
    }
}
