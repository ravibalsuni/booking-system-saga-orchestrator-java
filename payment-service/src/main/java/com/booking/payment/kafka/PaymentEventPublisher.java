package com.booking.payment.kafka;

import com.booking.payment.config.KafkaTopics;
import com.booking.payment.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(BaseEvent event) {
        log.info("Publishing event [{}] to topic [{}], traceId={}",
                event.getEventType(), KafkaTopics.PAYMENT_REPLIES, event.getTraceId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_REPLIES, event);
    }
}
