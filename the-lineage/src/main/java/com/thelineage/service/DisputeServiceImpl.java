package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.DisputeRepository;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputes;
    private final OrderRepository orders;
    private final UserRepository users;

    public DisputeServiceImpl(DisputeRepository disputes, OrderRepository orders, UserRepository users) {
        this.disputes = disputes;
        this.orders = orders;
        this.users = users;
    }

    @Override
    @Transactional
    public Dispute open(UUID orderId, UUID raisedByUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Reason is required");
        }
        OrderEntity order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        User raisedBy = users.findById(raisedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + raisedByUserId));
        if (!order.getBuyer().getId().equals(raisedByUserId)) {
            throw new ForbiddenException("Only the buyer can raise a dispute");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new ConflictException("Cannot dispute a completed or refunded order");
        }
        order.setStatus(OrderStatus.DISPUTED);
        orders.save(order);
        return disputes.save(Dispute.builder()
                .order(order)
                .raisedBy(raisedBy)
                .status(DisputeStatus.OPEN)
                .reason(reason)
                .build());
    }

    @Override
    @Transactional
    public Dispute resolve(UUID disputeId, UUID adminUserId, DisputeStatus outcome, String note) {
        Dispute dispute = disputes.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("Dispute not found: " + disputeId));
        User admin = users.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + adminUserId));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only admins can resolve disputes");
        }
        if (outcome != DisputeStatus.RESOLVED_BUYER && outcome != DisputeStatus.RESOLVED_SELLER) {
            throw new BadRequestException("Outcome must be RESOLVED_BUYER or RESOLVED_SELLER");
        }
        if (dispute.getStatus() != DisputeStatus.OPEN && dispute.getStatus() != DisputeStatus.UNDER_REVIEW) {
            throw new ConflictException("Dispute already resolved");
        }
        dispute.setStatus(outcome);
        dispute.setResolver(admin);
        dispute.setResolutionNote(note);
        dispute.setResolvedAt(Instant.now());
        OrderEntity order = dispute.getOrder();
        order.setStatus(outcome == DisputeStatus.RESOLVED_BUYER ? OrderStatus.REFUNDED : OrderStatus.COMPLETED);
        if (outcome == DisputeStatus.RESOLVED_SELLER) order.setCompletedAt(Instant.now());
        orders.save(order);
        return disputes.save(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findByOrder(UUID orderId) {
        return disputes.findAllByOrderId(orderId);
    }
}
