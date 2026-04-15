package com.booking.reservation.event.command;


import com.booking.reservation.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateReservationCommand extends BaseEvent {

    private UUID reservationId;
    private LocalDate date;
    private LocalTime time;
    private String guestName;
    private String guestEmail;

    public CreateReservationCommand(UUID reservationId, LocalDate date, LocalTime time,
                                    String guestName, String guestEmail, String traceId) {
        super(traceId, "CreateReservationCommand", Instant.now());
        this.reservationId = reservationId;
        this.date = date;
        this.time = time;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
    }
}
