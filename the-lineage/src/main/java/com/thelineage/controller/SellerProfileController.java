package com.thelineage.controller;

import com.thelineage.domain.SellerProfile;
import com.thelineage.service.SellerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/sellers")
@Tag(name = "Sellers", description = "Public seller profiles")
public class SellerProfileController {

    private final SellerProfileService profiles;

    public SellerProfileController(SellerProfileService profiles) {
        this.profiles = profiles;
    }

    public record SellerPublicDto(UUID id, String displayName, String tier, String bio,
                                  String applicationStatus, Instant approvedAt) {}

    @GetMapping("/{id}")
    @Operation(summary = "Public seller profile")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seller profile returned."),
            @ApiResponse(responseCode = "404", description = "No seller profile with that id.", content = @Content)
    })
    public SellerPublicDto get(@PathVariable UUID id) {
        SellerProfile p = profiles.findById(id);
        return new SellerPublicDto(
                p.getId(),
                p.getUser().getDisplayName(),
                p.getTier().name(),
                p.getBio(),
                p.getApplicationStatus().name(),
                p.getApprovedAt()
        );
    }
}
