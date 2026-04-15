package com.booking.orchestrator.event.reply;

import com.booking.orchestrator.event.BaseEvent;
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
public class ReservationCancelled extends BaseEvent {

    private UUID reservationId;

    public ReservationCancelled(UUID reservationId, String traceId) {
        super(traceId, "ReservationCancelled", Instant.now());
        this.reservationId = reservationId;
    }
}
