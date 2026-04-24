package com.thelineage.controller;

import com.thelineage.domain.SellerApplication;
import com.thelineage.dto.seller.ApplicationData;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.SellerApplicationService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sellers/applications")
@Tag(name = "Seller Applications", description = "Apply to become a seller; curators review the queue")
public class SellerApplicationController {

    private final SellerApplicationService service;

    public SellerApplicationController(SellerApplicationService service) {
        this.service = service;
    }

    public record ApplicationDto(UUID id, UUID applicantId, String status, Instant submittedAt) {}

    @PostMapping
    @Operation(
            summary = "Submit a seller application",
            description = "Any authenticated user can submit. Only one PENDING application per user is permitted; " +
                    "submit again only after the previous one is reviewed."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application submitted in PENDING state."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Applicant already has a PENDING application.",
                    content = @Content)
    })
    public ResponseEntity<ApplicationDto> submit(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @Valid @RequestBody ApplicationData body
    ) {
        SellerApplication app = service.submit(principal.id(), body);
        return ResponseEntity.status(201).body(new ApplicationDto(
                app.getId(), app.getApplicant().getId(), app.getStatus().name(), app.getSubmittedAt()));
    }

    @GetMapping("/pending")
    @Operation(
            summary = "List all pending seller applications",
            description = "Intended for curators triaging the application queue."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of pending applications."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content)
    })
    public List<ApplicationDto> listPending() {
        return service.listPending().stream()
                .map(a -> new ApplicationDto(a.getId(), a.getApplicant().getId(), a.getStatus().name(), a.getSubmittedAt()))
                .toList();
    }
}
