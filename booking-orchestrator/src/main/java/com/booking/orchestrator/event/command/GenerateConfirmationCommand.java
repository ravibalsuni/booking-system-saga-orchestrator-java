package com.booking.orchestrator.event.command;

import com.booking.orchestrator.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GenerateConfirmationCommand extends BaseEvent {

    private UUID reservationId;
    private UUID paymentId;
    private String guestName;
    private String guestEmail;

    public GenerateConfirmationCommand(UUID reservationId, UUID paymentId,
                                       String guestName, String guestEmail, String traceId) {
        super(traceId, "GenerateConfirmationCommand", Instant.now());
        this.reservationId = reservationId;
        this.paymentId = paymentId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
    }
}