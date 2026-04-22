package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.listing.CreateListingRequest;
import com.thelineage.dto.listing.ListingFilter;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.ShoeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listings;
    private final ShoeRepository shoes;
    private final SellerProfileRepository profiles;
    private final ProvenanceService provenance;

    public ListingServiceImpl(ListingRepository listings,
                              ShoeRepository shoes,
                              SellerProfileRepository profiles,
                              ProvenanceService provenance) {
        this.listings = listings;
        this.shoes = shoes;
        this.profiles = profiles;
        this.provenance = provenance;
    }

    @Override
    @Transactional
    public Listing create(UUID sellerUserId, CreateListingRequest request) {
        SellerProfile profile = profiles.findByUserId(sellerUserId)
                .orElseThrow(() -> new ForbiddenException("Seller profile not found"));
        if (profile.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new ForbiddenException("Seller application not approved");
        }
        Shoe shoe = shoes.findById(request.shoeId())
                .orElseThrow(() -> new NotFoundException("Shoe not found: " + request.shoeId()));
        if (!shoe.getSeller().getId().equals(profile.getId())) {
            throw new ForbiddenException("Shoe does not belong to seller");
        }
        if (!shoe.isAuthenticated()) {
            throw new BadRequestException("Shoe is not authenticated yet");
        }
        Listing listing = Listing.builder()
                .shoe(shoe)
                .seller(profile)
                .price(request.price())
                .currency(request.currency())
                .state(ListingState.AVAILABLE)
                .build();
        Listing saved = listings.save(listing);
        provenance.append(shoe.getId(), sellerUserId, ProvenanceEventType.LISTED,
                "{\"listingId\":\"" + saved.getId() + "\",\"price\":\"" + saved.getPrice() + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public Listing reserve(UUID listingId) {
        Listing listing = requireListing(listingId);
        if (listing.getState() != ListingState.AVAILABLE) {
            throw new ConflictException("Listing is not available");
        }
        listing.setState(ListingState.RESERVED);
        listing.setStateChangedAt(Instant.now());
        return listings.save(listing);
    }

    @Override
    @Transactional
    public Listing markSold(UUID listingId, UUID buyerUserId) {
        Listing listing = requireListing(listingId);
        if (listing.getState() != ListingState.RESERVED) {
            throw new ConflictException("Listing must be RESERVED to be sold");
        }
        listing.setState(ListingState.SOLD);
        listing.setStateChangedAt(Instant.now());
        Listing saved = listings.save(listing);
        provenance.append(listing.getShoe().getId(), buyerUserId, ProvenanceEventType.SOLD,
                "{\"listingId\":\"" + listingId + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public Listing unlist(UUID listingId, UUID sellerUserId) {
        Listing listing = requireListing(listingId);
        SellerProfile profile = profiles.findByUserId(sellerUserId)
                .orElseThrow(() -> new ForbiddenException("Seller profile not found"));
        if (!listing.getSeller().getId().equals(profile.getId())) {
            throw new ForbiddenException("Only the owner can unlist");
        }
        if (listing.getState() == ListingState.SOLD) {
            throw new ConflictException("Sold listings cannot be unlisted");
        }
        listing.setState(ListingState.UNLISTED);
        listing.setStateChangedAt(Instant.now());
        Listing saved = listings.save(listing);
        provenance.append(listing.getShoe().getId(), sellerUserId, ProvenanceEventType.UNLISTED,
                "{\"listingId\":\"" + listingId + "\"}");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Listing findById(UUID listingId) {
        return requireListing(listingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Listing> search(ListingFilter filter) {
        int page = filter.page() >= 0 ? filter.page() : 0;
        int size = filter.size() > 0 ? Math.min(filter.size(), 100) : 20;
        return listings.findAllByState(ListingState.AVAILABLE, PageRequest.of(page, size));
    }

    private Listing requireListing(UUID id) {
        return listings.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found: " + id));
    }
}
