package com.booking.orchestrator.event.command;

import com.booking.orchestrator.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProcessPaymentCommand extends BaseEvent {

    private UUID reservationId;
    private BigDecimal amount;
    private String paymentMethod;

    public ProcessPaymentCommand(UUID reservationId, BigDecimal amount,
                                 String paymentMethod, String traceId) {
        super(traceId, "ProcessPaymentCommand", Instant.now());
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
