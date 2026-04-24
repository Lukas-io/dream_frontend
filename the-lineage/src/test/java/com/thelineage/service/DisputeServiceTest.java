package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.repository.DisputeRepository;
import com.thelineage.repository.OrderRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock private DisputeRepository disputes;
    @Mock private OrderRepository orders;
    @Mock private UserRepository users;
    @Mock private ProvenanceService provenance;
    @InjectMocks private DisputeServiceImpl service;

    private OrderEntity orderFor(UUID orderId, User buyer, OrderStatus status, UUID shoeId) {
        Shoe shoe = Shoe.builder().id(shoeId).build();
        Listing listing = Listing.builder().id(UUID.randomUUID()).shoe(shoe).build();
        return OrderEntity.builder().id(orderId).buyer(buyer).listing(listing).status(status).build();
    }

    @Test
    void open_byBuyer_setsOrderDisputedAndSavesOpenDisputeAndAppendsProvenance() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        OrderEntity order = orderFor(orderId, buyer, OrderStatus.SHIPPED, shoeId);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputes.save(any())).thenAnswer(inv -> {
            Dispute d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        Dispute d = service.open(orderId, buyerId, "never arrived");
        assertThat(d.getStatus()).isEqualTo(DisputeStatus.OPEN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DISPUTED);
        verify(provenance).append(eq(shoeId), eq(buyerId), eq(ProvenanceEventType.DISPUTED), any());
    }

    @Test
    void open_byNonBuyer_throwsForbidden() {
        UUID orderId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        OrderEntity order = orderFor(orderId, User.builder().id(UUID.randomUUID()).build(),
                OrderStatus.PAID, UUID.randomUUID());
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(callerId)).thenReturn(Optional.of(User.builder().id(callerId).build()));
        assertThatThrownBy(() -> service.open(orderId, callerId, "mine!")).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void open_onCompletedOrder_throwsConflict() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        OrderEntity order = orderFor(orderId, buyer, OrderStatus.COMPLETED, UUID.randomUUID());
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        assertThatThrownBy(() -> service.open(orderId, buyerId, "r")).isInstanceOf(ConflictException.class);
    }

    @Test
    void resolve_byAdminForBuyer_refundsOrderAndAppendsResolvedProvenance() {
        UUID disputeId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID shoeId = UUID.randomUUID();
        OrderEntity order = orderFor(UUID.randomUUID(),
                User.builder().id(UUID.randomUUID()).build(),
                OrderStatus.DISPUTED, shoeId);
        Dispute d = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN).order(order).build();
        User admin = User.builder().id(adminId).role(UserRole.ADMIN).build();
        when(disputes.findById(disputeId)).thenReturn(Optional.of(d));
        when(users.findById(adminId)).thenReturn(Optional.of(admin));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputes.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Dispute resolved = service.resolve(disputeId, adminId, DisputeStatus.RESOLVED_BUYER, "refund");
        assertThat(resolved.getStatus()).isEqualTo(DisputeStatus.RESOLVED_BUYER);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        verify(provenance).append(eq(shoeId), eq(adminId), eq(ProvenanceEventType.RESOLVED), any());
    }

    @Test
    void resolve_byNonAdmin_throwsForbidden() {
        UUID disputeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Dispute d = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN)
                .order(orderFor(UUID.randomUUID(),
                        User.builder().id(UUID.randomUUID()).build(),
                        OrderStatus.DISPUTED, UUID.randomUUID()))
                .build();
        when(disputes.findById(disputeId)).thenReturn(Optional.of(d));
        when(users.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).role(UserRole.BUYER).build()));
        assertThatThrownBy(() -> service.resolve(disputeId, userId, DisputeStatus.RESOLVED_BUYER, "x"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void resolve_withInvalidOutcome_throwsBadRequest() {
        UUID disputeId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Dispute d = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN)
                .order(orderFor(UUID.randomUUID(),
                        User.builder().id(UUID.randomUUID()).build(),
                        OrderStatus.DISPUTED, UUID.randomUUID()))
                .build();
        when(disputes.findById(disputeId)).thenReturn(Optional.of(d));
        when(users.findById(adminId)).thenReturn(Optional.of(User.builder().id(adminId).role(UserRole.ADMIN).build()));
        assertThatThrownBy(() -> service.resolve(disputeId, adminId, DisputeStatus.OPEN, "nope"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void open_withBlankReason_throwsBadRequest() {
        assertThatThrownBy(() -> service.open(UUID.randomUUID(), UUID.randomUUID(), "  "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findByOrder_delegates() {
        UUID orderId = UUID.randomUUID();
        when(disputes.findAllByOrderId(orderId)).thenReturn(List.of(new Dispute()));
        assertThat(service.findByOrder(orderId)).hasSize(1);
    }

    private static <T> T eq(T t) { return org.mockito.ArgumentMatchers.eq(t); }
}
