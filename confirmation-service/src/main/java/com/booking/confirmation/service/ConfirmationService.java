package com.booking.confirmation.service;

import com.booking.confirmation.entity.Confirmation;
import com.booking.confirmation.entity.DeliveryStatus;
import com.booking.confirmation.event.reply.ConfirmationFailed;
import com.booking.confirmation.event.reply.ConfirmationGenerated;
import com.booking.confirmation.kafka.ConfirmationEventPublisher;
import com.booking.confirmation.repo.ConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;
    private final ConfirmationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Transactional
    public void generateConfirmation(UUID reservationId, UUID paymentId,
                                     String guestName, String guestEmail, String traceId) {
        log.info("Generating confirmation for reservationId={}, traceId={}", reservationId, traceId);

        try {
            String confirmationNumber = "CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Confirmation confirmation = new Confirmation();
            confirmation.setReservationId(reservationId);
            confirmation.setPaymentId(paymentId);
            confirmation.setConfirmationNumber(confirmationNumber);
            confirmation.setGuestName(guestName);
            confirmation.setGuestEmail(guestEmail);
            confirmation.setDeliveryStatus(DeliveryStatus.SENT);

            try {
                emailService.sendConfirmationEmail(confirmation);
            } catch (Exception e) {
                log.error("Email delivery failed for reservationId={}, traceId={}", reservationId, traceId, e);
                confirmation.setDeliveryStatus(DeliveryStatus.DELIVERY_FAILED);
            }

            confirmation = confirmationRepository.save(confirmation);
            log.info("Confirmation generated id={}, number={}, reservationId={}, traceId={}",
                    confirmation.getId(), confirmationNumber, reservationId, traceId);
            eventPublisher.publish(new ConfirmationGenerated(reservationId, confirmationNumber, traceId));

        } catch (Exception e) {
            log.error("Confirmation generation failed for reservationId={}, traceId={}", reservationId, traceId, e);
            eventPublisher.publish(new ConfirmationFailed(reservationId, e.getMessage(), traceId));
        }
    }
}