package com.thelineage.repository;

import com.thelineage.domain.Listing;
import com.thelineage.domain.ListingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Page<Listing> findAllByState(ListingState state, Pageable pageable);
    List<Listing> findAllBySellerId(UUID sellerProfileId);
}
