package com.thelineage.controller;

import com.thelineage.domain.ConditionGrade;
import com.thelineage.domain.Listing;
import com.thelineage.dto.PageResponse;
import com.thelineage.dto.listing.*;
import com.thelineage.mapper.ListingMapper;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.ListingService;
import com.thelineage.service.ProvenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/listings")
@Tag(name = "Listings", description = "Public catalog browse + seller listing management")
public class ListingController {

    private final ListingService listings;
    private final ProvenanceService provenance;
    private final ListingMapper mapper;

    public ListingController(ListingService listings, ProvenanceService provenance, ListingMapper mapper) {
        this.listings = listings;
        this.provenance = provenance;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
            summary = "Search the public catalog of available listings",
            description = "Returns a paginated set of listings currently in the AVAILABLE state."
    )
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Listings page returned.")})
    public PageResponse<ListingDto> search(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String colorway,
            @RequestParam(required = false) Integer eraFrom,
            @RequestParam(required = false) Integer eraTo,
            @RequestParam(required = false) ConditionGrade condition,
            @RequestParam(required = false) Integer minRarity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListingFilter filter = new ListingFilter(brand, colorway, eraFrom, eraTo, condition, minRarity, page, size);
        Page<Listing> result = listings.search(filter);
        return PageResponse.from(result, mapper::toDto);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Fetch a listing with its full provenance chain (passport)",
            description = "Public endpoint. The passport is returned in chronological order (oldest first)."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing detail returned."),
            @ApiResponse(responseCode = "404", description = "No listing with that id.", content = @Content)
    })
    public ListingDetailDto get(@PathVariable UUID id) {
        Listing listing = listings.findById(id);
        List<ProvenanceRecordDto> chain = provenance.chainFor(listing.getShoe().getId()).stream()
                .map(mapper::toDto)
                .toList();
        return new ListingDetailDto(mapper.toDto(listing), chain);
    }

    @PostMapping
    @Operation(
            summary = "Seller creates a listing for a previously authenticated shoe",
            description = "Requires SELLER role with an APPROVED application status. The shoe must already be " +
                    "authenticated by a curator. Appends a LISTED provenance record."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Listing created in AVAILABLE state."),
            @ApiResponse(responseCode = "400", description = "Validation failed or shoe not yet authenticated.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an approved seller, or shoe belongs to another seller.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Shoe not found.", content = @Content)
    })
    public ResponseEntity<ListingDto> create(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @Valid @RequestBody CreateListingRequest request
    ) {
        Listing listing = listings.create(principal.id(), request);
        return ResponseEntity.created(URI.create("/listings/" + listing.getId()))
                .body(mapper.toDto(listing));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Seller unlists their own listing",
            description = "Transitions the listing to UNLISTED. Sold listings cannot be unlisted."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listing unlisted."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the listing owner.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Listing is SOLD and cannot be unlisted.",
                    content = @Content)
    })
    public ResponseEntity<ListingDto> unlist(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @PathVariable UUID id
    ) {
        Listing listing = listings.unlist(id, principal.id());
        return ResponseEntity.ok(mapper.toDto(listing));
    }
}
