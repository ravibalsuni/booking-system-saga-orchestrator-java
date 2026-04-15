package com.booking.orchestrator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "booking_sagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaState state;

    private String failureReason;
    private String traceId;

    // Request data fields
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod;

    // IDs populated during saga execution
    private UUID reservationId;
    private UUID paymentId;
    private String confirmationNumber;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (sagaId == null) {
            sagaId = UUID.randomUUID().toString();
        }
        if (state == null) {
            state = SagaState.STARTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}