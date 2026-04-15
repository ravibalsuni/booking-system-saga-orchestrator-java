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
public class ReservationFailed extends BaseEvent {

    private UUID reservationId;
    private String reason;

    public ReservationFailed(UUID reservationId, String reason, String traceId) {
        super(traceId, "ReservationFailed", Instant.now());
        this.reservationId = reservationId;
        this.reason = reason;
    }
}
