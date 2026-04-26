package com.thelineage.service;

import com.thelineage.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {
    Payment initiate(UUID orderId);
    Payment confirm(UUID paymentId, String processorReference);
    Payment releaseEscrow(UUID paymentId);
    Payment refund(UUID paymentId, String reason);
    Optional<Payment> findByOrderId(UUID orderId);
}
