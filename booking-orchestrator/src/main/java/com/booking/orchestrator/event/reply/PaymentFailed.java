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
public class PaymentFailed extends BaseEvent {

    private UUID reservationId;
    private String reason;

    public PaymentFailed(UUID reservationId, String reason, String traceId) {
        super(traceId, "PaymentFailed", Instant.now());
        this.reservationId = reservationId;
        this.reason = reason;
    }
}