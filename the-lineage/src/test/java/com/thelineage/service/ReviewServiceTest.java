package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.review.ReviewScores;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.ReviewRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviews;
    @Mock private OrderRepository orders;
    @Mock private UserRepository users;
    @InjectMocks private ReviewServiceImpl service;

    @Test
    void submit_whenOrderCompletedAndCallerBuyer_persistsReview() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        SellerProfile seller = SellerProfile.builder().id(UUID.randomUUID()).build();
        OrderEntity order = OrderEntity.builder().id(orderId).buyer(buyer).seller(seller).status(OrderStatus.COMPLETED).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(reviews.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Review r = service.submit(orderId, buyerId, new ReviewScores(5, 4, 5), "great");
        assertThat(r.getAccuracyScore()).isEqualTo(5);
        assertThat(r.getOrder()).isSameAs(order);
    }

    @Test
    void submit_whenCallerNotBuyer_throwsForbidden() {
        UUID orderId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        User someone = User.builder().id(otherUser).build();
        OrderEntity order = OrderEntity.builder().id(orderId)
                .buyer(User.builder().id(UUID.randomUUID()).build())
                .seller(SellerProfile.builder().id(UUID.randomUUID()).build())
                .status(OrderStatus.COMPLETED).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(otherUser)).thenReturn(Optional.of(someone));
        assertThatThrownBy(() -> service.submit(orderId, otherUser, new ReviewScores(5,5,5), "x"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void submit_whenOrderNotCompleted_throwsConflict() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        OrderEntity order = OrderEntity.builder().id(orderId)
                .buyer(buyer).seller(SellerProfile.builder().id(UUID.randomUUID()).build())
                .status(OrderStatus.PAID).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        assertThatThrownBy(() -> service.submit(orderId, buyerId, new ReviewScores(5,5,5), "x"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findByListing_delegates() {
        UUID listingId = UUID.randomUUID();
        when(reviews.findAllByOrder_Listing_Id(listingId)).thenReturn(List.of(new Review()));
        assertThat(service.findByListing(listingId)).hasSize(1);
    }

    @Test
    void findBySeller_delegates() {
        UUID sellerId = UUID.randomUUID();
        when(reviews.findAllBySellerId(sellerId)).thenReturn(List.of(new Review()));
        assertThat(service.findBySeller(sellerId)).hasSize(1);
    }
}
