package com.booking.confirmation.kafka;

import com.booking.confirmation.config.KafkaTopics;
import com.booking.confirmation.service.ConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmationCommandListener {

    private final ConfirmationService confirmationService;

    @KafkaListener(topics = KafkaTopics.CONFIRMATION_COMMANDS,
            groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        String traceId = (String) message.get("traceId");
        log.info("Received command [{}], traceId={}", eventType, traceId);

        switch (eventType) {
            case "GenerateConfirmationCommand" -> handleGenerateConfirmation(message, traceId);
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

    private void handleGenerateConfirmation(Map<String, Object> message, String traceId) {
        UUID reservationId = UUID.fromString((String) message.get("reservationId"));
        UUID paymentId = UUID.fromString((String) message.get("paymentId"));
        String guestName = (String) message.get("guestName");
        String guestEmail = (String) message.get("guestEmail");
        confirmationService.generateConfirmation(reservationId, paymentId, guestName, guestEmail, traceId);
    }
}
