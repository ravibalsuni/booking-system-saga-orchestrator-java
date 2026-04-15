package com.booking.confirmation.kafka;

import com.booking.confirmation.config.KafkaTopics;
import com.booking.confirmation.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(BaseEvent event) {
        log.info("Publishing event [{}] to topic [{}], traceId={}",
                event.getEventType(), KafkaTopics.CONFIRMATION_REPLIES, event.getTraceId());
        kafkaTemplate.send(KafkaTopics.CONFIRMATION_REPLIES, event);
    }
}
