package com.thelineage.service;

import com.thelineage.domain.Listing;
import com.thelineage.dto.listing.CreateListingRequest;
import com.thelineage.dto.listing.ListingFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ListingService {
    Listing create(UUID sellerUserId, CreateListingRequest request);
    Listing reserve(UUID listingId);
    Listing markSold(UUID listingId, UUID buyerUserId);
    Listing unlist(UUID listingId, UUID sellerUserId);
    Listing findById(UUID listingId);
    Page<Listing> search(ListingFilter filter);
}
