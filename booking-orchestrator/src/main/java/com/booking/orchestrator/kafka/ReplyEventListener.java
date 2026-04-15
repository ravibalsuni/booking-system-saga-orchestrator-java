package com.booking.orchestrator.kafka;

import com.booking.orchestrator.config.KafkaTopics;
import com.booking.orchestrator.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyEventListener {

    private final SagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = KafkaTopics.RESERVATION_REPLIES,
            groupId = "orchestrator-group")
    public void onReservationReply(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received reservation reply: {}", eventType);

        switch (eventType) {
            case "ReservationCreated" -> sagaOrchestrator.handleReservationCreated(event);
            case "ReservationFailed" -> sagaOrchestrator.handleReservationFailed(event);
            case "ReservationCancelled" -> sagaOrchestrator.handleReservationCancelled(event);
            case "ReservationUpdated" -> log.info("Reservation updated: {}", event.get("reservationId"));
            default -> log.warn("Unknown reservation reply event type: {}", eventType);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REPLIES, groupId = "orchestrator-group")
    public void onPaymentReply(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received payment reply: {}", eventType);

        switch (eventType) {
            case "PaymentCompleted" -> sagaOrchestrator.handlePaymentCompleted(event);
            case "PaymentFailed" -> sagaOrchestrator.handlePaymentFailed(event);
            case "RefundCompleted" -> sagaOrchestrator.handleRefundCompleted(event);
            case "RefundFailed" -> sagaOrchestrator.handleRefundFailed(event);
            default -> log.warn("Unknown payment reply event type: {}", eventType);
        }
    }

    @KafkaListener(topics = KafkaTopics.CONFIRMATION_REPLIES, groupId = "orchestrator-group")
    public void onConfirmationReply(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received confirmation reply: {}", eventType);

        switch (eventType) {
            case "ConfirmationGenerated" -> sagaOrchestrator.handleConfirmationGenerated(event);
            case "ConfirmationFailed" -> sagaOrchestrator.handleConfirmationFailed(event);
            default -> log.warn("Unknown confirmation reply event type: {}", eventType);
        }
    }
}
