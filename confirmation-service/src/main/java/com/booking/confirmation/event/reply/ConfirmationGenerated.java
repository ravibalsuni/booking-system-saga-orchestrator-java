package com.booking.confirmation.event.reply;

import com.booking.confirmation.event.BaseEvent;
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
public class ConfirmationGenerated extends BaseEvent {

    private UUID reservationId;
    private String confirmationNumber;

    public ConfirmationGenerated(UUID reservationId, String confirmationNumber, String traceId) {
        super(traceId, "ConfirmationGenerated", Instant.now());
        this.reservationId = reservationId;
        this.confirmationNumber = confirmationNumber;
    }
}
