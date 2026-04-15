package com.booking.reservation.event.reply;

import com.booking.reservation.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReservationCreated extends BaseEvent {

    private UUID reservationId;
    private String status;

    public ReservationCreated(UUID reservationId, String status, String traceId) {
        super(traceId, "ReservationCreated", Instant.now());
        this.reservationId = reservationId;
        this.status = status;
    }
}
