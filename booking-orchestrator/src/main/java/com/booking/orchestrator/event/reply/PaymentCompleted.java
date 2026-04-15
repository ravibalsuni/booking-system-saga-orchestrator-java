package com.booking.orchestrator.event.reply;

import com.booking.orchestrator.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCompleted extends BaseEvent {

    private UUID reservationId;
    private UUID paymentId;
    private BigDecimal amount;

    public PaymentCompleted(UUID reservationId, UUID paymentId, BigDecimal amount, String traceId) {
        super(traceId, "PaymentCompleted", Instant.now());
        this.reservationId = reservationId;
        this.paymentId = paymentId;
        this.amount = amount;
    }
}
