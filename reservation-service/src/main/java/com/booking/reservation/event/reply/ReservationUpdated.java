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
public class ReservationUpdated extends BaseEvent {

    private UUID reservationId;

    public ReservationUpdated(UUID reservationId, String traceId) {
        super(traceId, "ReservationUpdated", Instant.now());
        this.reservationId = reservationId;
    }
}