package com.booking.orchestrator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Command topics
    @Bean
    public NewTopic reservationCommandsTopic() {
        return TopicBuilder.name(KafkaTopics.RESERVATION_COMMANDS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCommandsTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMMANDS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic confirmationCommandsTopic() {
        return TopicBuilder.name(KafkaTopics.CONFIRMATION_COMMANDS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Reply topics
    @Bean
    public NewTopic reservationRepliesTopic() {
        return TopicBuilder.name(KafkaTopics.RESERVATION_REPLIES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentRepliesTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_REPLIES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic confirmationRepliesTopic() {
        return TopicBuilder.name(KafkaTopics.CONFIRMATION_REPLIES)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Dead Letter Topics
    @Bean
    public NewTopic reservationCommandsDltTopic() {
        return TopicBuilder.name(KafkaTopics.RESERVATION_COMMANDS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reservationRepliesDltTopic() {
        return TopicBuilder.name(KafkaTopics.RESERVATION_REPLIES_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCommandsDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_COMMANDS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentRepliesDltTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_REPLIES_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic confirmationCommandsDltTopic() {
        return TopicBuilder.name(KafkaTopics.CONFIRMATION_COMMANDS_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic confirmationRepliesDltTopic() {
        return TopicBuilder.name(KafkaTopics.CONFIRMATION_REPLIES_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}