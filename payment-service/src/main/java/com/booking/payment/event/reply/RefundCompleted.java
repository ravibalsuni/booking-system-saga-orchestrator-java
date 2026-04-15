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
public class RefundCompleted extends BaseEvent {

    private UUID reservationId;
    private UUID paymentId;

    public RefundCompleted(UUID reservationId, UUID paymentId, String traceId) {
        super(traceId, "RefundCompleted", Instant.now());
        this.reservationId = reservationId;
        this.paymentId = paymentId;
    }
}
