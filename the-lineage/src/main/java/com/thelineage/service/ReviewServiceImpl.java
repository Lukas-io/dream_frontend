package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.review.ReviewScores;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.ReviewRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviews;
    private final OrderRepository orders;
    private final UserRepository users;

    public ReviewServiceImpl(ReviewRepository reviews, OrderRepository orders, UserRepository users) {
        this.reviews = reviews;
        this.orders = orders;
        this.users = users;
    }

    @Override
    @Transactional
    public Review submit(UUID orderId, UUID authorUserId, ReviewScores scores, String body) {
        OrderEntity order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        User author = users.findById(authorUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorUserId));
        if (!order.getBuyer().getId().equals(authorUserId)) {
            throw new ForbiddenException("Only the buyer may review this order");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ConflictException("Cannot review incomplete order");
        }
        if (reviews.existsByOrderId(orderId)) {
            throw new ConflictException("Review already submitted for this order");
        }
        return reviews.save(Review.builder()
                .order(order)
                .author(author)
                .seller(order.getSeller())
                .accuracyScore(scores.accuracyScore())
                .conditionScore(scores.conditionScore())
                .shippingScore(scores.shippingScore())
                .body(body)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByListing(UUID listingId) {
        return reviews.findAllByOrder_Listing_Id(listingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findBySeller(UUID sellerProfileId) {
        return reviews.findAllBySellerId(sellerProfileId);
    }
}
