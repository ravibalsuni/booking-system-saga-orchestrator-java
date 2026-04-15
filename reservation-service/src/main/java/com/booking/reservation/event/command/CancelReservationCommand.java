package com.booking.reservation.event.command;

import com.booking.reservation.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CancelReservationCommand extends BaseEvent {

    private UUID reservationId;
    private String traceId;
    private String reason;

    public CancelReservationCommand(UUID reservationId, String traceId) {
        super(traceId, "CancelReservationCommand", Instant.now());
        this.reservationId = reservationId;
        this.traceId = traceId;
    }
}