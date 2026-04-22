package com.thelineage.service;

import com.thelineage.domain.EscrowStatus;
import com.thelineage.domain.OrderEntity;
import com.thelineage.domain.Payment;
import com.thelineage.domain.PaymentStatus;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository payments;
    private final OrderRepository orders;

    public PaymentServiceImpl(PaymentRepository payments, OrderRepository orders) {
        this.payments = payments;
        this.orders = orders;
    }

    @Override
    @Transactional
    public Payment initiate(UUID orderId) {
        OrderEntity order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        payments.findByOrderId(orderId).ifPresent(existing -> {
            throw new ConflictException("Payment already exists for order");
        });
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentStatus(PaymentStatus.INITIATED)
                .escrowStatus(EscrowStatus.HELD)
                .build();
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment confirm(UUID paymentId, String processorReference) {
        Payment payment = requirePayment(paymentId);
        if (payment.getPaymentStatus() != PaymentStatus.INITIATED
                && payment.getPaymentStatus() != PaymentStatus.AUTHORIZED) {
            throw new ConflictException("Payment cannot be confirmed from status: " + payment.getPaymentStatus());
        }
        payment.setProcessorReference(processorReference);
        payment.setPaymentStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(Instant.now());
        payment.setEscrowStatus(EscrowStatus.HELD);
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment releaseEscrow(UUID paymentId) {
        Payment payment = requirePayment(paymentId);
        if (payment.getPaymentStatus() != PaymentStatus.CAPTURED) {
            throw new ConflictException("Cannot release escrow for a non-captured payment");
        }
        if (payment.getEscrowStatus() != EscrowStatus.HELD) {
            throw new ConflictException("Escrow is not currently HELD");
        }
        payment.setEscrowStatus(EscrowStatus.RELEASED);
        payment.setEscrowReleasedAt(Instant.now());
        return payments.save(payment);
    }

    @Override
    @Transactional
    public Payment refund(UUID paymentId, String reason) {
        Payment payment = requirePayment(paymentId);
        if (payment.getEscrowStatus() != EscrowStatus.HELD) {
            throw new ConflictException("Only HELD escrow can be refunded");
        }
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setEscrowStatus(EscrowStatus.REVERSED);
        return payments.save(payment);
    }

    private Payment requirePayment(UUID paymentId) {
        return payments.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Payment> findByOrderId(UUID orderId) {
        return payments.findByOrderId(orderId);
    }
}
