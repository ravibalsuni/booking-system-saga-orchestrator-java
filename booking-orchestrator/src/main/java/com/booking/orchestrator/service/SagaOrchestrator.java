package com.booking.orchestrator.service;

import com.booking.orchestrator.config.KafkaTopics;
import com.booking.orchestrator.entity.BookingSaga;
import com.booking.orchestrator.entity.SagaState;
import com.booking.orchestrator.event.command.CreateReservationCommand;
import com.booking.orchestrator.repository.BookingSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
}
