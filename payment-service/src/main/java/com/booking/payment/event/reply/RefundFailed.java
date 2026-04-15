package com.booking.payment.event.reply;

import com.booking.payment.event.BaseEvent;
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
public class RefundFailed extends BaseEvent {

    private UUID reservationId;
    private String reason;

    public RefundFailed(UUID reservationId, String reason, String traceId) {
        super(traceId, "RefundFailed", Instant.now());
        this.reservationId = reservationId;
        this.reason = reason;
    }
}