package com.booking.reservation.kafka;

import com.booking.reservation.config.KafkaTopics;
import com.booking.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCommandListener {

    private final ReservationService reservationService;

    @KafkaListener(topics = KafkaTopics.RESERVATION_COMMANDS,
            groupId = "${spring.kafka.consumer.group-id}")
    public void onCommand(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        String traceId = (String) message.get("traceId");
        log.info("Received command ({}), traceId={}", eventType, traceId);

        switch (eventType) {
            case "CreateReservationCommand" -> handleCreate(message, traceId);
            case "CancelReservationCommand" -> handleCancel(message, traceId);
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCreate(Map<String, Object> message, String traceId) {
        UUID reservationId = UUID.fromString((String) message.get("reservationId"));
        LocalDate date = parseDate(message.get("date"));
        LocalTime time = parseTime(message.get("time"));
        String guestName = (String) message.get("guestName");
        String guestEmail = (String) message.get("guestEmail");
        reservationService.createReservation(reservationId, date, time, guestName, guestEmail, traceId);
    }

    private LocalDate parseDate(Object value) {
        if (value instanceof String s) {
            return LocalDate.parse(s);
        } else if (value instanceof java.util.List<?> list) {
            return LocalDate.of((int) list.get(0), (int) list.get(1), (int) list.get(2));
        }
        throw new IllegalArgumentException("Cannot parse date from: " + value);
    }

    private LocalTime parseTime(Object value) {
        if (value instanceof String s) {
            return LocalTime.parse(s);
        } else if (value instanceof java.util.List<?> list) {
            int hour = (int) list.get(0);
            int minute = (int) list.get(1);
            int second = list.size() > 2 ? (int) list.get(2) : 0;
            return LocalTime.of(hour, minute, second);
        }
        throw new IllegalArgumentException("Cannot parse time from: " + value);
    }

    private void handleCancel(Map<String, Object> message, String traceId) {
        UUID reservationId = UUID.fromString((String) message.get("reservationId"));
        reservationService.cancelReservation(reservationId, traceId);
    }
}
