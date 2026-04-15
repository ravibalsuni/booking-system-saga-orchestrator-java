package com.booking.payment.service;

import com.booking.payment.entity.Payment;
import com.booking.payment.entity.PaymentStatus;
import com.booking.payment.event.reply.PaymentCompleted;
import com.booking.payment.event.reply.PaymentFailed;
import com.booking.payment.event.reply.RefundCompleted;
import com.booking.payment.event.reply.RefundFailed;
import com.booking.payment.kafka.PaymentEventPublisher;
import com.booking.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public void processPayment(UUID reservationId, BigDecimal amount, String paymentMethod, String traceId) {
        log.info("Processing payment for reservationId={}, amount={}, traceId={}", reservationId, amount, traceId);
        try {
            Payment payment = new Payment();
            payment.setReservationId(reservationId);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment = paymentRepository.save(payment);
            log.info("Payment completed id={}, reservationId={}, traceId={}", payment.getId(), reservationId, traceId);
            eventPublisher.publish(new PaymentCompleted(reservationId, payment.getId(), amount, traceId));
        } catch (Exception e) {
            log.error("Payment processing failed for reservationId={}, traceId={}", reservationId, traceId, e);
            Payment failedPayment = new Payment();
            failedPayment.setReservationId(reservationId);
            failedPayment.setAmount(amount);
            failedPayment.setPaymentMethod(paymentMethod);
            failedPayment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(failedPayment);
            eventPublisher.publish(new PaymentFailed(reservationId, e.getMessage(), traceId));
        }
    }

    @Transactional
    public void processRefund(UUID reservationId, UUID paymentId, String traceId) {
        log.info("Processing refund for reservationId={}, paymentId={}, traceId={}", reservationId, paymentId, traceId);
        try {
            Payment refund = new Payment();
            refund.setReservationId(reservationId);
            refund.setAmount(BigDecimal.ZERO);
            refund.setPaymentMethod("REFUND");
            refund.setStatus(PaymentStatus.REFUNDED);
            refund = paymentRepository.save(refund);
            log.info("Refund completed id={}, reservationId={}, traceId={}", refund.getId(), reservationId, traceId);
            eventPublisher.publish(new RefundCompleted(reservationId, refund.getId(), traceId));
        } catch (Exception e) {
            log.error("Refund processing failed for reservationId={}, traceId={}", reservationId, traceId, e);
            eventPublisher.publish(new RefundFailed(reservationId, e.getMessage(), traceId));
        }
    }
}