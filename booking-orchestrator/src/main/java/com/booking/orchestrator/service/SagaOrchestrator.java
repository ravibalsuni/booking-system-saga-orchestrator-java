package com.booking.orchestrator.service;

import com.booking.orchestrator.config.KafkaTopics;
import com.booking.orchestrator.entity.BookingSaga;
import com.booking.orchestrator.entity.SagaState;
import com.booking.orchestrator.event.command.*;
import com.booking.orchestrator.repository.BookingSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final BookingSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public BookingSaga initiateSaga(LocalDate date, LocalTime time, String guestName,
                                    String guestEmail, BigDecimal amount,
                                    String paymentMethod, String traceId) {

        BookingSaga saga = new BookingSaga();
        saga.setState(SagaState.STARTED);
        saga.setTraceId(traceId);
        saga.setDate(date);
        saga.setTime(time);
        saga.setGuestName(guestName);
        saga.setGuestEmail(guestEmail);
        saga.setAmount(amount);
        saga.setPaymentMethod(paymentMethod);
        saga = sagaRepository.save(saga); // save to generate sagaId via @PrePersist

        UUID reservationId = UUID.randomUUID();
        saga.setReservationId(reservationId);
        saga = sagaRepository.save(saga);

        log.info("Saga {} initiated with state STARTED, traceId={}", saga.getId(), traceId);
        CreateReservationCommand command = new CreateReservationCommand(reservationId, date, time, guestName, guestEmail, traceId);
        kafkaTemplate.send(KafkaTopics.RESERVATION_COMMANDS, reservationId.toString(), command);
        return saga;
    }

    @Transactional
    public void handleReservationCreated(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        if (saga.getState() != SagaState.STARTED) {
            log.warn("Saga {} in unexpected state {} for ReservationCreated, traceId={}",
                    saga.getId(), saga.getState(), traceId);
            return;
        }

        saga.setState(SagaState.RESERVATION_CREATED);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.info("Saga {} transitioned to RESERVATION_CREATED, traceId={}", saga.getId(), traceId);

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                reservationId, saga.getAmount(), saga.getPaymentMethod(), traceId);

        kafkaTemplate.send(KafkaTopics.PAYMENT_COMMANDS, reservationId.toString(), command);
    }

    @Transactional
    public void handleReservationFailed(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");
        String reason = (String) event.get("reason");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        saga.setState(SagaState.FAILED);
        saga.setFailureReason(reason);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);
        log.info("Saga {} transitioned to FAILED (reservation failed: {}), traceId={}",
                saga.getId(), reason, traceId);
    }

    @Transactional
    public void handlePaymentCompleted(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        UUID paymentId = UUID.fromString((String) event.get("paymentId"));
        String traceId = (String) event.get("traceId");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        if (saga.getState() != SagaState.RESERVATION_CREATED) {
            log.warn("Saga {} in unexpected state {} for PaymentCompleted, traceId={}", saga.getId(), saga.getState(), traceId);
            return;
        }

        saga.setState(SagaState.PAYMENT_COMPLETED);
        saga.setPaymentId(paymentId);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.info("Saga {} transitioned to PAYMENT_COMPLETED, traceId={}", saga.getId(), traceId);

        GenerateConfirmationCommand command = new GenerateConfirmationCommand(
                reservationId, paymentId, saga.getGuestName(), saga.getGuestEmail(), traceId);

        kafkaTemplate.send(KafkaTopics.CONFIRMATION_COMMANDS, reservationId.toString(), command);
    }

    @Transactional
    public void handlePaymentFailed(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");
        String reason = (String) event.get("reason");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        saga.setState(SagaState.COMPENSATING);
        saga.setFailureReason(reason);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.info("Saga {} transitioned to COMPENSATING (payment failed), traceId={}", saga.getId(), traceId);

        CancelReservationCommand command = new CancelReservationCommand(reservationId, traceId);
        kafkaTemplate.send(KafkaTopics.RESERVATION_COMMANDS, reservationId.toString(), command);
    }

    @Transactional
    public void handleConfirmationGenerated(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String confirmationNumber = (String) event.get("confirmationNumber");
        String traceId = (String) event.get("traceId");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }
        BookingSaga saga = optSaga.get();

        if (saga.getState() != SagaState.PAYMENT_COMPLETED) {
            log.warn("Saga {} in unexpected state {} for ConfirmationGenerated, traceId={}",
                    saga.getId(), saga.getState(), traceId);
            return;
        }

        saga.setState(SagaState.CONFIRMED);
        saga.setConfirmationNumber(confirmationNumber);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.info("Saga {} transitioned to CONFIRMED, traceId={}", saga.getId(), traceId);
    }

    @Transactional
    public void handleConfirmationFailed(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");
        String reason = (String) event.get("reason");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        saga.setState(SagaState.COMPENSATING);
        saga.setFailureReason(reason);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.info("Saga {} transitioned to COMPENSATING (confirmation failed), traceId={}", saga.getId(), traceId);

// Compensate: refund payment and cancel reservation
        RefundPaymentCommand refundCommand = new RefundPaymentCommand(
                reservationId, saga.getPaymentId(), traceId);
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMMANDS, reservationId.toString(), refundCommand);

        CancelReservationCommand cancelCommand = new CancelReservationCommand(reservationId, traceId);
        kafkaTemplate.send(KafkaTopics.RESERVATION_COMMANDS, reservationId.toString(), cancelCommand);
    }

    @Transactional
    public void handleReservationCancelled(Map<String, Object> event) {
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        if (saga.getState() == SagaState.COMPENSATING) {
            // Check if all compensations are done
            // For payment-failed compensation: only cancel reservation needed
            // For confirmation-failed compensation: refund + cancel needed
            // If we had a paymentId, we need refund too; otherwise just cancel suffices
            if (saga.getPaymentId() == null || saga.getFailureReason() != null) {
                // Simple compensation (payment failed path) - cancel was the only compensation
                saga.setState(SagaState.COMPENSATED);
                saga.setUpdatedAt(Instant.now());
                sagaRepository.save(saga);
                log.info("Saga {} transitioned to COMPENSATED, traceId={}", saga.getId(), traceId);
                // If paymentId exists and confirmation failed, we wait for refund too
                // The refund handler will finalize
            } else {
                // Direct cancellation (user-initiated)
                saga.setUpdatedAt(Instant.now());
                sagaRepository.save(saga);
                log.info("Saga {} reservation cancelled, traceId={}", saga.getId(), traceId);
            }
        }
    }

    @Transactional
    public void handleRefundCompleted (Map < String, Object > event){
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }

        BookingSaga saga = optSaga.get();

        if (saga.getState() == SagaState.COMPENSATING) {
            saga.setState(SagaState.COMPENSATED);
            saga.setUpdatedAt(Instant.now());
            sagaRepository.save(saga);
            log.info("Saga {} transitioned to COMPENSATED (refund completed), traceId={}", saga.getId(), traceId);
        }
    }

    @Transactional
    public void handleRefundFailed (Map < String, Object > event){
        UUID reservationId = UUID.fromString((String) event.get("reservationId"));
        String traceId = (String) event.get("traceId");
        String reason = (String) event.get("reason");

        Optional<BookingSaga> optSaga = sagaRepository.findByReservationId(reservationId);
        if (optSaga.isEmpty()) {
            log.warn("No saga found for reservationId={}, traceId={}", reservationId, traceId);
            return;
        }


        BookingSaga saga = optSaga.get();

        saga.setState(SagaState.FAILED);
        saga.setFailureReason("Compensation failed: " + reason);
        saga.setUpdatedAt(Instant.now());
        sagaRepository.save(saga);

        log.error("Saga {} transitioned to FAILED (refund failed: {}), traceId={}", saga.getId(), reason, traceId);
    }

    @Transactional
    public void cancelBooking (BookingSaga saga){
        String traceId = saga.getTraceId();
        UUID reservationId = saga.getReservationId();

        if (saga.getState() == SagaState.CONFIRMED) {
            // Need to refund and cancel
            saga.setState(SagaState.COMPENSATING);
            saga.setUpdatedAt(Instant.now());
            sagaRepository.save(saga);

            RefundPaymentCommand refundCommand = new RefundPaymentCommand(
                    reservationId, saga.getPaymentId(), traceId);
            kafkaTemplate.send(KafkaTopics.PAYMENT_COMMANDS, reservationId.toString(), refundCommand);
            CancelReservationCommand cancelCommand = new CancelReservationCommand(reservationId, traceId);
            kafkaTemplate.send(KafkaTopics.RESERVATION_COMMANDS, reservationId.toString(), cancelCommand);

        } else if (saga.getState() == SagaState.STARTED || saga.getState() == SagaState.RESERVATION_CREATED) {
            // Just cancel the reservation
            CancelReservationCommand cancelCommand = new CancelReservationCommand(reservationId, traceId);
            kafkaTemplate.send(KafkaTopics.RESERVATION_COMMANDS, reservationId.toString(), cancelCommand);
        }
    }
}
