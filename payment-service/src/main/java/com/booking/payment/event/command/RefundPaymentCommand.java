package com.booking.payment.event.command;

import com.booking.payment.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefundPaymentCommand extends BaseEvent {

    private UUID reservationId;
    private UUID paymentId;

    public RefundPaymentCommand(UUID reservationId, UUID paymentId, String traceId) {
        super(traceId, "RefundPaymentCommand", Instant.now());
        this.reservationId = reservationId;
        this.paymentId = paymentId;
    }
}