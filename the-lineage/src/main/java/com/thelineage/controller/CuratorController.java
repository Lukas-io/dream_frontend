package com.thelineage.controller;

import com.thelineage.domain.SellerApplication;
import com.thelineage.domain.Shoe;
import com.thelineage.dto.seller.ApproveApplicationRequest;
import com.thelineage.dto.seller.RejectApplicationRequest;
import com.thelineage.dto.shoe.AuthenticateShoeRequest;
import com.thelineage.dto.shoe.ShoeDto;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.SellerApplicationService;
import com.thelineage.service.ShoeService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/curator")
@Tag(name = "Curator", description = "Curator workflow — applications + shoe authentication")
public class CuratorController {

    private final SellerApplicationService applications;
    private final ShoeService shoes;
    private final DomainMappers mappers;

    public CuratorController(SellerApplicationService applications, ShoeService shoes, DomainMappers mappers) {
        this.applications = applications;
        this.shoes = shoes;
        this.mappers = mappers;
    }

    public record ApplicationDto(UUID id, UUID applicantId, String status, Instant submittedAt, Instant reviewedAt) {}

    @GetMapping("/applications/pending")
    @Operation(summary = "List pending seller applications")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending applications."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a CURATOR or ADMIN.", content = @Content)
    })
    public List<ApplicationDto> pending() {
        return applications.listPending().stream()
                .map(a -> new ApplicationDto(a.getId(), a.getApplicant().getId(),
                        a.getStatus().name(), a.getSubmittedAt(), a.getReviewedAt()))
                .toList();
    }

    @PostMapping("/applications/{id}/approve")
    @Operation(summary = "Approve a seller application and assign a tier; promotes applicant to SELLER")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application approved; applicant promoted."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a CURATOR or ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application or curator user not found.",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Application is not in PENDING state.",
                    content = @Content)
    })
    public ApplicationDto approve(@AuthenticationPrincipal LineageUserPrincipal principal,
                                  @PathVariable UUID id,
                                  @Valid @RequestBody ApproveApplicationRequest body) {
        SellerApplication a = applications.approve(id, principal.id(), body.tier(), body.note());
        return new ApplicationDto(a.getId(), a.getApplicant().getId(), a.getStatus().name(),
                a.getSubmittedAt(), a.getReviewedAt());
    }

    @PostMapping("/applications/{id}/reject")
    @Operation(summary = "Reject a seller application")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application rejected."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a CURATOR or ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application or curator user not found.",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Application is not in PENDING state.",
                    content = @Content)
    })
    public ApplicationDto reject(@AuthenticationPrincipal LineageUserPrincipal principal,
                                 @PathVariable UUID id,
                                 @Valid @RequestBody RejectApplicationRequest body) {
        SellerApplication a = applications.reject(id, principal.id(), body.note());
        return new ApplicationDto(a.getId(), a.getApplicant().getId(), a.getStatus().name(),
                a.getSubmittedAt(), a.getReviewedAt());
    }

    @PostMapping("/shoes/{shoeId}/authenticate")
    @Operation(
            summary = "Authenticate a submitted shoe — grade condition and set rarity score",
            description = "Appends an AUTHENTICATED provenance record. Once authenticated, the seller may list the shoe."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shoe authenticated."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a CURATOR or ADMIN.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Shoe or curator user not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Shoe is already authenticated.", content = @Content)
    })
    public ShoeDto authenticate(@AuthenticationPrincipal LineageUserPrincipal principal,
                                @PathVariable UUID shoeId,
                                @Valid @RequestBody AuthenticateShoeRequest body) {
        Shoe s = shoes.authenticate(shoeId, principal.id(), body.conditionGrade(), body.rarityScore());
        return mappers.toDto(s);
    }
}
