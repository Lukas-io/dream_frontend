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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock private DisputeRepository disputes;
    @Mock private OrderRepository orders;
    @Mock private UserRepository users;
    @InjectMocks private DisputeServiceImpl service;

    @Test
    void open_byBuyer_setsOrderDisputedAndSavesOpenDispute() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        OrderEntity order = OrderEntity.builder().id(orderId).buyer(buyer).status(OrderStatus.SHIPPED).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputes.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Dispute d = service.open(orderId, buyerId, "never arrived");
        assertThat(d.getStatus()).isEqualTo(DisputeStatus.OPEN);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DISPUTED);
    }

    @Test
    void open_byNonBuyer_throwsForbidden() {
        UUID orderId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(orderId)
                .buyer(User.builder().id(UUID.randomUUID()).build()).status(OrderStatus.PAID).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(callerId)).thenReturn(Optional.of(User.builder().id(callerId).build()));
        assertThatThrownBy(() -> service.open(orderId, callerId, "mine!")).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void open_onCompletedOrder_throwsConflict() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        OrderEntity order = OrderEntity.builder().id(orderId).buyer(buyer).status(OrderStatus.COMPLETED).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        assertThatThrownBy(() -> service.open(orderId, buyerId, "r")).isInstanceOf(ConflictException.class);
    }

    @Test
    void resolve_byAdminForBuyer_refundsOrder() {
        UUID disputeId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(UUID.randomUUID()).status(OrderStatus.DISPUTED).build();
        Dispute d = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN).order(order).build();
        User admin = User.builder().id(adminId).role(UserRole.ADMIN).build();
        when(disputes.findById(disputeId)).thenReturn(Optional.of(d));
        when(users.findById(adminId)).thenReturn(Optional.of(admin));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputes.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Dispute resolved = service.resolve(disputeId, adminId, DisputeStatus.RESOLVED_BUYER, "refund");
        assertThat(resolved.getStatus()).isEqualTo(DisputeStatus.RESOLVED_BUYER);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void resolve_byNonAdmin_throwsForbidden() {
        UUID disputeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Dispute d = Dispute.builder().id(disputeId).status(DisputeStatus.OPEN)
                .order(OrderEntity.builder().id(UUID.randomUUID()).status(OrderStatus.DISPUTED).build()).build();
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
                .order(OrderEntity.builder().id(UUID.randomUUID()).status(OrderStatus.DISPUTED).build()).build();
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
}
