package com.booking.orchestrator.controller;

import com.booking.orchestrator.entity.BookingSaga;
import com.booking.orchestrator.repository.BookingSagaRepository;
import com.booking.orchestrator.service.SagaOrchestrator;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final SagaOrchestrator sagaOrchestrator;
    private final BookingSagaRepository sagaRepository;
    private final Tracer tracer;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> request) {
        String traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "no-trace";
        log.info("Creating booking with traceId={}", traceId);

        // Validate required fields
        List<String> errors = validateCreateRequest(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "details", errors
            ));
        }

        try {
            LocalDate date = LocalDate.parse((String) request.get("date"));
            LocalTime time = LocalTime.parse((String) request.get("time"));
            String guestName = (String) request.get("guestName");
            String guestEmail = (String) request.get("guestEmail");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");

            BookingSaga saga = sagaOrchestrator.initiateSaga(
                    date, time, guestName, guestEmail, amount, paymentMethod, traceId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sagaId", saga.getSagaId());
            response.put("reservationId", saga.getReservationId());
            response.put("state", saga.getState());
            response.put("traceId", traceId);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid date/time format",
                    "details", "Use ISO-8601: date=2026-04-15, time=14:30:00"
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid amount",
                    "details", "amount must be a valid decimal number"
            ));
        }
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<?> getBookingStatus(@PathVariable String sagaId) {
        return sagaRepository.findBySagaId(sagaId)
                .map(saga -> ResponseEntity.ok(Map.of(
                        "sagaId", saga.getSagaId(),
                        "state", saga.getState(),
                        "reservationId", saga.getReservationId(),
                        "paymentId", saga.getPaymentId(),
                        "confirmationId", saga.getConfirmationId(),
                        "errorMessage", saga.getErrorMessage(),
                        "createdAt", saga.getCreatedAt(),
                        "updatedAt", saga.getUpdatedAt()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private List<String> validateCreateRequest(Map<String, Object> request) {
        List<String> errors = new java.util.ArrayList<>();
        if (request.get("date") == null) errors.add("date is required");
        if (request.get("time") == null) errors.add("time is required");
        if (request.get("guestName") == null || request.get("guestName").toString().isBlank())
            errors.add("guestName is required");
        if (request.get("guestEmail") == null || request.get("guestEmail").toString().isBlank())
            errors.add("guestEmail is required");
        if (request.get("amount") == null) errors.add("amount is required");
        if (request.get("paymentMethod") == null || request.get("paymentMethod").toString().isBlank())
            errors.add("paymentMethod is required");
        return errors;
    }
}
