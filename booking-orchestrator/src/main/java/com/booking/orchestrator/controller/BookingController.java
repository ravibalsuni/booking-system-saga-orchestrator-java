package com.booking.orchestrator.controller;

import com.booking.orchestrator.entity.BookingSaga;
import com.booking.orchestrator.entity.SagaState;
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
import java.util.*;

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
        // Validate required fields
        List<String> errors = validateCreateRequest(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "details", errors
            ));
        }

        String traceId = getTraceId();

        LocalDate date = LocalDate.parse((String) request.get("date"));
        LocalTime time = LocalTime.parse((String) request.get("time"));
        String guestName = (String) request.get("guestName");
        String guestEmail = (String) request.get("guestEmail");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String paymentMethod = (String) request.get("paymentMethod");

        BookingSaga saga = sagaOrchestrator.initiateSaga(
                date, time, guestName, guestEmail, amount, paymentMethod, traceId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sagaId", saga.getId());
        response.put("reservationId", saga.getReservationId());
        response.put("state", saga.getState());
        response.put("traceId", traceId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingSaga>> getBookingsByEmail(@RequestParam String email) {
        List<BookingSaga> sagas = sagaRepository.findByGuestEmailOrderByDateAsc(email);
        return ResponseEntity.ok(sagas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingSaga> getBooking(@PathVariable UUID id) {
        return sagaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable UUID id, @RequestBody Map<String, Object> request) {
        Optional<BookingSaga> optSaga = sagaRepository.findById(id);
        if (optSaga.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BookingSaga saga = optSaga.get();

        // Only PENDING (STARTED/RESERVATION_CREATED) sagas can be updated
        if (saga.getState() == SagaState.CONFIRMED || saga.getState() == SagaState.FAILED
                || saga.getState() == SagaState.COMPENSATING || saga.getState() == SagaState.COMPENSATED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Reservation cannot be modified in current status"
            ));
        }

        // Validate update fields
        List<String> errors = validateUpdateRequest(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validation failed",
                    "details", errors
            ));
        }

        if (request.containsKey("date")) {
            saga.setDate(LocalDate.parse((String) request.get("date")));
        }
        if (request.containsKey("time")) {
            saga.setTime(LocalTime.parse((String) request.get("time")));
        }
        if (request.containsKey("guestName")) {
            saga.setGuestName((String) request.get("guestName"));
        }
        if (request.containsKey("guestEmail")) {
            saga.setGuestEmail((String) request.get("guestEmail"));
        }

        saga = sagaRepository.save(saga);
        return ResponseEntity.ok(saga);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id) {
        Optional<BookingSaga> optSaga = sagaRepository.findById(id);
        if (optSaga.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BookingSaga saga = optSaga.get();

        if (saga.getState() == SagaState.FAILED || saga.getState() == SagaState.COMPENSATED
                || saga.getState() == SagaState.COMPENSATING) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Reservation is already cancelled"
            ));
        }

        sagaOrchestrator.cancelBooking(saga);
        return ResponseEntity.ok(Map.of("message", "Cancellation initiated", "sagaId", saga.getId()));
    }

    @GetMapping("/{id}/confirmation")
    public ResponseEntity<?> getConfirmation(@PathVariable UUID id) {
        Optional<BookingSaga> optSaga = sagaRepository.findById(id);
        if (optSaga.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BookingSaga saga = optSaga.get();

        if (saga.getConfirmationNumber() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Confirmation not found"
            ));
        }

        Map<String, Object> confirmation = new LinkedHashMap<>();
        confirmation.put("sagaId", saga.getId());
        confirmation.put("reservationId", saga.getReservationId());
        confirmation.put("confirmationNumber", saga.getConfirmationNumber());
        confirmation.put("guestName", saga.getGuestName());
        confirmation.put("guestEmail", saga.getGuestEmail());
        confirmation.put("date", saga.getDate());
        confirmation.put("time", saga.getTime());

        return ResponseEntity.ok(confirmation);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getBookingStatus(@PathVariable UUID id) {
        Optional<BookingSaga> optSaga = sagaRepository.findById(id);
        if (optSaga.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BookingSaga saga = optSaga.get();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("sagaId", saga.getId());
        status.put("state", saga.getState());
        status.put("failureReason", saga.getFailureReason());
        status.put("createdAt", saga.getCreatedAt());
        status.put("updatedAt", saga.getUpdatedAt());

        return ResponseEntity.ok(status);
    }

    private List<String> validateCreateRequest(Map<String, Object> request) {
        List<String> errors = new ArrayList<>();

        if (request.get("date") == null || request.get("date").toString().isBlank()) {
            errors.add("date is required");
        }
        if (request.get("time") == null || request.get("time").toString().isBlank()) {
            errors.add("time is required");
        }
        if (request.get("guestName") == null || request.get("guestName").toString().isBlank()) {
            errors.add("guestName is required");
        }
        if (request.get("guestEmail") == null || request.get("guestEmail").toString().isBlank()) {
            errors.add("guestEmail is required");
        }
        if (request.get("amount") == null) {
            errors.add("amount is required");
        }
        if (request.get("paymentMethod") == null || request.get("paymentMethod").toString().isBlank()) {
            errors.add("paymentMethod is required");
        }

        return errors;
    }

    private List<String> validateUpdateRequest(Map<String, Object> request) {
        List<String> errors = new ArrayList<>();

        if (request.containsKey("guestName") && request.get("guestName").toString().isBlank()) {
            errors.add("guestName cannot be blank");
        }
        if (request.containsKey("guestEmail") && request.get("guestEmail").toString().isBlank()) {
            errors.add("guestEmail cannot be blank");
        }

        return errors;
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return String.valueOf(tracer.currentSpan().context().traceId());
        }
        return UUID.randomUUID().toString();
    }
}