package com.booking.reservation.kafka;

import com.booking.reservation.config.KafkaTopics;
import com.booking.reservation.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(BaseEvent event) {
        log.info("Publishing event [{}] to topic [{}], traceId={}",
                event.getEventType(), KafkaTopics.RESERVATION_REPLIES, event.getTraceId());
        kafkaTemplate.send(KafkaTopics.RESERVATION_REPLIES, event);
    }
}
