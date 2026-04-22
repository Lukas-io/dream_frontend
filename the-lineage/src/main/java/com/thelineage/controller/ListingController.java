package com.thelineage.controller;

import com.thelineage.domain.ConditionGrade;
import com.thelineage.domain.Listing;
import com.thelineage.dto.listing.*;
import com.thelineage.mapper.ListingMapper;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.ListingService;
import com.thelineage.service.ProvenanceService;
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
    public List<ListingDto> search(
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
        return result.getContent().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ListingDetailDto get(@PathVariable UUID id) {
        Listing listing = listings.findById(id);
        List<ProvenanceRecordDto> chain = provenance.chainFor(listing.getShoe().getId()).stream()
                .map(mapper::toDto)
                .toList();
        return new ListingDetailDto(mapper.toDto(listing), chain);
    }

    @PostMapping
    public ResponseEntity<ListingDto> create(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @Valid @RequestBody CreateListingRequest request
    ) {
        Listing listing = listings.create(principal.id(), request);
        return ResponseEntity.created(URI.create("/listings/" + listing.getId()))
                .body(mapper.toDto(listing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ListingDto> unlist(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @PathVariable UUID id
    ) {
        Listing listing = listings.unlist(id, principal.id());
        return ResponseEntity.ok(mapper.toDto(listing));
    }
}
