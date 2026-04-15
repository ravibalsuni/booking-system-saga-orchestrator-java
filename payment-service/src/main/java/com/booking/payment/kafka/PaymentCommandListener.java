package com.booking.payment.kafka;

import com.booking.payment.config.KafkaTopics;
import com.booking.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMMANDS,
            groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        String traceId = (String) message.get("traceId");
        log.info("Received command [{}], traceId={}", eventType, traceId);

        switch (eventType) {
            case "ProcessPaymentCommand" -> handleProcessPayment(message, traceId);
            case "RefundPaymentCommand" -> handleRefund(message, traceId);
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

    private void handleProcessPayment(Map<String, Object> message, String traceId) {
        UUID reservationId = UUID.fromString((String) message.get("reservationId"));
        BigDecimal amount = new BigDecimal(message.get("amount").toString());
        String paymentMethod = (String) message.get("paymentMethod");
        paymentService.processPayment(reservationId, amount, paymentMethod, traceId);
    }

    private void handleRefund(Map<String, Object> message, String traceId) {
        UUID reservationId = UUID.fromString((String) message.get("reservationId"));
        UUID paymentId = UUID.fromString((String) message.get("paymentId"));
        paymentService.processRefund(reservationId, paymentId, traceId);
    }

}
