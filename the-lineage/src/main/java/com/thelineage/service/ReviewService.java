package com.thelineage.service;

import com.thelineage.domain.Review;
import com.thelineage.dto.review.ReviewScores;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    Review submit(UUID orderId, UUID authorUserId, ReviewScores scores, String body);
    List<Review> findByListing(UUID listingId);
    List<Review> findBySeller(UUID sellerProfileId);
}
