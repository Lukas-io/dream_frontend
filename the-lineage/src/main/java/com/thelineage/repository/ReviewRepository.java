package com.thelineage.repository;

import com.thelineage.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findAllBySellerId(UUID sellerProfileId);
    List<Review> findAllByOrder_Listing_Id(UUID listingId);
    boolean existsByOrderId(UUID orderId);
}
