package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.ConflictException;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository payments;
    @Mock private OrderRepository orders;
    @InjectMocks private PaymentServiceImpl service;

    @Test
    void initiate_whenNoExistingPayment_createsHeldEscrow() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(orderId).totalAmount(new BigDecimal("100")).currency("USD").build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Payment p = service.initiate(orderId);
        assertThat(p.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(p.getEscrowStatus()).isEqualTo(EscrowStatus.HELD);
    }

    @Test
    void initiate_whenPaymentExists_throwsConflict() {
        UUID orderId = UUID.randomUUID();
        when(orders.findById(orderId)).thenReturn(Optional.of(OrderEntity.builder().id(orderId).build()));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(new Payment()));
        assertThatThrownBy(() -> service.initiate(orderId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void confirm_movesFromInitiatedToCaptured() {
        UUID paymentId = UUID.randomUUID();
        Payment p = Payment.builder().id(paymentId).paymentStatus(PaymentStatus.INITIATED)
                .escrowStatus(EscrowStatus.HELD).build();
        when(payments.findById(paymentId)).thenReturn(Optional.of(p));
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Payment result = service.confirm(paymentId, "ref-1");
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(result.getProcessorReference()).isEqualTo("ref-1");
        assertThat(result.getCapturedAt()).isNotNull();
    }

    @Test
    void releaseEscrow_whenCapturedAndHeld_marksReleased() {
        UUID paymentId = UUID.randomUUID();
        Payment p = Payment.builder().id(paymentId).paymentStatus(PaymentStatus.CAPTURED)
                .escrowStatus(EscrowStatus.HELD).build();
        when(payments.findById(paymentId)).thenReturn(Optional.of(p));
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Payment result = service.releaseEscrow(paymentId);
        assertThat(result.getEscrowStatus()).isEqualTo(EscrowStatus.RELEASED);
    }

    @Test
    void releaseEscrow_whenNotCaptured_throwsConflict() {
        UUID paymentId = UUID.randomUUID();
        Payment p = Payment.builder().id(paymentId).paymentStatus(PaymentStatus.INITIATED).build();
        when(payments.findById(paymentId)).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> service.releaseEscrow(paymentId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void refund_whenEscrowHeld_reversesAndMarksRefunded() {
        UUID paymentId = UUID.randomUUID();
        Payment p = Payment.builder().id(paymentId).paymentStatus(PaymentStatus.CAPTURED)
                .escrowStatus(EscrowStatus.HELD).build();
        when(payments.findById(paymentId)).thenReturn(Optional.of(p));
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Payment result = service.refund(paymentId, "buyer confirmed fake");
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.getEscrowStatus()).isEqualTo(EscrowStatus.REVERSED);
    }

    @Test
    void refund_whenEscrowReleased_throwsConflict() {
        UUID paymentId = UUID.randomUUID();
        Payment p = Payment.builder().id(paymentId).paymentStatus(PaymentStatus.CAPTURED)
                .escrowStatus(EscrowStatus.RELEASED).build();
        when(payments.findById(paymentId)).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> service.refund(paymentId, "too late")).isInstanceOf(ConflictException.class);
    }
}
