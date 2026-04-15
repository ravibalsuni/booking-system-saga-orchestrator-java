package com.booking.confirmation.service;

import com.booking.confirmation.entity.Confirmation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendConfirmationEmail(Confirmation confirmation) {
        log.info("========== CONFIRMATION EMAIL ==========");
        log.info("To: {}", confirmation.getGuestEmail());
        log.info("Guest: {}", confirmation.getGuestName());
        log.info("Confirmation Number: {}", confirmation.getConfirmationNumber());
        log.info("Reservation ID: {}", confirmation.getReservationId());
        log.info("Payment ID: {}", confirmation.getPaymentId());
        log.info("========================================");
    }
}
