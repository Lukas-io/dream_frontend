package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.listing.CreateListingRequest;
import com.thelineage.dto.listing.ListingFilter;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.ShoeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock private ListingRepository listings;
    @Mock private ShoeRepository shoes;
    @Mock private SellerProfileRepository profiles;
    @Mock private ProvenanceService provenance;
    @InjectMocks private ListingServiceImpl service;

    private SellerProfile approvedProfile(UUID profileId) {
        return SellerProfile.builder().id(profileId).applicationStatus(ApplicationStatus.APPROVED).build();
    }

    private Shoe authenticatedShoe(UUID id, SellerProfile seller) {
        return Shoe.builder().id(id).seller(seller)
                .authenticatedAt(Instant.now())
                .authenticatedByCurator(User.builder().id(UUID.randomUUID()).build())
                .build();
    }

    @Test
    void createListing_whenSellerApprovedAndShoeAuthenticated_createsAvailableAndAppendsProvenance() {
        UUID sellerUserId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        SellerProfile profile = approvedProfile(profileId);
        Shoe shoe = authenticatedShoe(shoeId, profile);
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(profile));
        when(shoes.findById(shoeId)).thenReturn(Optional.of(shoe));
        when(listings.save(any())).thenAnswer(inv -> {
            Listing l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        Listing result = service.create(sellerUserId,
                new CreateListingRequest(shoeId, new BigDecimal("1200.00"), "USD"));

        assertThat(result.getState()).isEqualTo(ListingState.AVAILABLE);
        verify(provenance).append(shoeId, sellerUserId, ProvenanceEventType.LISTED,
                "{\"listingId\":\"" + result.getId() + "\",\"price\":\"1200.00\"}");
    }

    @Test
    void createListing_whenSellerNotApproved_throwsForbiddenException() {
        UUID sellerUserId = UUID.randomUUID();
        SellerProfile profile = SellerProfile.builder().applicationStatus(ApplicationStatus.PENDING).build();
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(profile));
        assertThatThrownBy(() -> service.create(sellerUserId,
                new CreateListingRequest(UUID.randomUUID(), new BigDecimal("10"), "USD")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createListing_whenShoeNotAuthenticated_throwsBadRequest() {
        UUID sellerUserId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        SellerProfile profile = approvedProfile(profileId);
        Shoe unauth = Shoe.builder().id(shoeId).seller(profile).build();
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(profile));
        when(shoes.findById(shoeId)).thenReturn(Optional.of(unauth));
        assertThatThrownBy(() -> service.create(sellerUserId,
                new CreateListingRequest(shoeId, new BigDecimal("10"), "USD")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createListing_whenShoeBelongsToOtherSeller_throwsForbidden() {
        UUID sellerUserId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        SellerProfile profile = approvedProfile(profileId);
        SellerProfile other = SellerProfile.builder().id(UUID.randomUUID()).applicationStatus(ApplicationStatus.APPROVED).build();
        Shoe foreign = authenticatedShoe(shoeId, other);
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(profile));
        when(shoes.findById(shoeId)).thenReturn(Optional.of(foreign));
        assertThatThrownBy(() -> service.create(sellerUserId,
                new CreateListingRequest(shoeId, new BigDecimal("10"), "USD")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void reserve_whenAvailable_movesToReserved() {
        UUID id = UUID.randomUUID();
        Listing listing = Listing.builder().id(id).state(ListingState.AVAILABLE).build();
        when(listings.findById(id)).thenReturn(Optional.of(listing));
        when(listings.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Listing result = service.reserve(id);
        assertThat(result.getState()).isEqualTo(ListingState.RESERVED);
    }

    @Test
    void reserve_whenNotAvailable_throwsConflict() {
        UUID id = UUID.randomUUID();
        Listing listing = Listing.builder().id(id).state(ListingState.SOLD).build();
        when(listings.findById(id)).thenReturn(Optional.of(listing));
        assertThatThrownBy(() -> service.reserve(id)).isInstanceOf(ConflictException.class);
    }

    @Test
    void markSold_fromReserved_movesToSoldAndAppendsProvenance() {
        UUID listingId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        Shoe shoe = Shoe.builder().id(shoeId).build();
        Listing listing = Listing.builder().id(listingId).shoe(shoe).state(ListingState.RESERVED).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(listings.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Listing result = service.markSold(listingId, buyerId);
        assertThat(result.getState()).isEqualTo(ListingState.SOLD);
        verify(provenance).append(eq(shoeId), eq(buyerId), eq(ProvenanceEventType.SOLD), any());
    }

    @Test
    void markSold_fromAvailable_throwsConflict() {
        UUID listingId = UUID.randomUUID();
        Listing listing = Listing.builder().id(listingId).state(ListingState.AVAILABLE).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        assertThatThrownBy(() -> service.markSold(listingId, UUID.randomUUID()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void unlist_whenCallerIsOwner_succeeds() {
        UUID listingId = UUID.randomUUID();
        UUID sellerUserId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        SellerProfile profile = SellerProfile.builder().id(profileId).build();
        Shoe shoe = Shoe.builder().id(UUID.randomUUID()).build();
        Listing listing = Listing.builder().id(listingId).state(ListingState.AVAILABLE).seller(profile).shoe(shoe).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(profile));
        when(listings.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Listing result = service.unlist(listingId, sellerUserId);
        assertThat(result.getState()).isEqualTo(ListingState.UNLISTED);
    }

    @Test
    void unlist_whenCallerIsNotOwner_throwsForbidden() {
        UUID listingId = UUID.randomUUID();
        UUID sellerUserId = UUID.randomUUID();
        SellerProfile other = SellerProfile.builder().id(UUID.randomUUID()).build();
        SellerProfile callerProfile = SellerProfile.builder().id(UUID.randomUUID()).build();
        Listing listing = Listing.builder().id(listingId).state(ListingState.AVAILABLE).seller(other).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(profiles.findByUserId(sellerUserId)).thenReturn(Optional.of(callerProfile));
        assertThatThrownBy(() -> service.unlist(listingId, sellerUserId)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void search_delegatesToRepoFilteredByAvailable() {
        Page<Listing> page = new PageImpl<>(List.of());
        when(listings.findAllByState(eq(ListingState.AVAILABLE), any())).thenReturn(page);
        Page<Listing> result = service.search(new ListingFilter(null, null, null, null, null, null, 0, 20));
        assertThat(result).isSameAs(page);
    }

    // helper to match any while keeping static import concise
    private static <T> T eq(T t) { return org.mockito.ArgumentMatchers.eq(t); }
}
