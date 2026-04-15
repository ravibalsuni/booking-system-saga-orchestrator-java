package com.booking.orchestrator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dead-letters")
@RequiredArgsConstructor
@Slf4j
public class DeadLetterController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerFactory<String, Object> consumerFactory;

    @GetMapping("/{topic}")
    public ResponseEntity<List<Map<String, Object>>> getDeadLetters(@PathVariable String topic) {
        String dltTopic = topic + "-dlt";
        List<Map<String, Object>> deadLetters = new ArrayList<>();

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer()) {
            List<TopicPartition> partitions = consumer.partitionsFor(dltTopic).stream()
                    .map(info -> new TopicPartition(dltTopic, info.partition()))
                    .toList();

            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));

            for (ConsumerRecord<String, Object> record : records) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("offset", record.offset());
                entry.put("partition", record.partition());
                entry.put("key", record.key());
                entry.put("value", record.value());
                entry.put("timestamp", record.timestamp());
                entry.put("topic", dltTopic);
                deadLetters.add(entry);
            }
        } catch (Exception e) {
            log.error("Error reading dead letters from topic {}: {}", dltTopic, e.getMessage());
            return ResponseEntity.ok(deadLetters);
        }
        return ResponseEntity.ok(deadLetters);
    }

    @PostMapping("/{topic}/{offset}/reprocess")
    public ResponseEntity<Map<String, Object>> reprocessDeadLetter(
            @PathVariable String topic, @PathVariable long offset) {
        String dltTopic = topic + "-dlt";
        String originalTopic = topic;

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer()) {
            List<TopicPartition> partitions = consumer.partitionsFor(dltTopic).stream()
                    .map(info -> new TopicPartition(dltTopic, info.partition()))
                    .toList();
            consumer.assign(partitions);

            for (TopicPartition partition : partitions) {
                consumer.seek(partition, offset);
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));

                for (ConsumerRecord<String, Object> record : records) {
                    if (record.offset() == offset) {
                        kafkaTemplate.send(originalTopic, record.key(), record.value());
                        log.info("Reprocessed dead letter from {} offset {} to {}",
                                dltTopic, offset, originalTopic);
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                                "offset", offset, "message", "Event reprocessed", "originalTopic", originalTopic
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reprocessing dead letter from {} offset {}: {}",
                    dltTopic, offset, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to reprocess dead letter",
                    "details", e.getMessage()
            ));
        }
        return ResponseEntity.notFound().build();
    }
}