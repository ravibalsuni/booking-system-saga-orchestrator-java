package com.booking.reservation.service;

import com.booking.reservation.entity.Reservation;
import com.booking.reservation.entity.ReservationStatus;
import com.booking.reservation.event.reply.ReservationCancelled;
import com.booking.reservation.event.reply.ReservationCreated;
import com.booking.reservation.event.reply.ReservationFailed;
import com.booking.reservation.event.reply.ReservationUpdated;
import com.booking.reservation.kafka.ReservationEventPublisher;
import com.booking.reservation.repo.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher eventPublisher;

    @Transactional
    public void createReservation(UUID reservationId, LocalDate date, LocalTime time,
                                  String guestName, String guestEmail, String traceId) {
        log.info("Creating reservation id={}, date={}, time={}, traceId={}", reservationId, date, time, traceId);

        // Validate future date
        if (date.isBefore(LocalDate.now())) {
            log.warn("Reservation date {} is in the past, traceId={}", date, traceId);
            eventPublisher.publish(new ReservationFailed(reservationId,
                    "Reservation date is in the past", traceId));
            return;
        }

        // Check slot availability
        if (reservationRepository.existsByDateAndTime(date, time)) {
            log.warn("Slot already booked for date={}, time={}, traceId={}", date, time, traceId);
            eventPublisher.publish(new ReservationFailed(reservationId,
                    "Time slot is unavailable", traceId));
            return;
        }
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setDate(date);
        reservation.setTime(time);
        reservation.setGuestName(guestName);
        reservation.setGuestEmail(guestEmail);
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation);
        log.info("Reservation created id={}, traceId={}", reservationId, traceId);

        eventPublisher.publish(new ReservationCreated(reservationId,
                ReservationStatus.PENDING.name(), traceId));
    }

    @Transactional
    public void updateReservation(UUID reservationId, LocalDate date, LocalTime time,
                                  String guestName, String guestEmail, String traceId) {
        log.info("Updating reservation id={}, traceId={}", reservationId, traceId);

        Optional<Reservation> optReservation = reservationRepository.findById(reservationId);
        if (optReservation.isEmpty()) {
            log.warn("Reservation not found id={}, traceId={}", reservationId, traceId);
            eventPublisher.publish(new ReservationFailed(reservationId,
                    "Reservation not found", traceId));
            return;
        }

        Reservation reservation = optReservation.get();
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            log.warn("Cannot update reservation in status={}, traceId={}", reservation.getStatus(), traceId);
            eventPublisher.publish(new ReservationFailed(reservationId,
                    "Reservation cannot be modified in current status", traceId));
            return;
        }

        if (date != null) reservation.setDate(date);
        if (time != null) reservation.setTime(time);
        if (guestName != null) reservation.setGuestName(guestName);
        if (guestEmail != null) reservation.setGuestEmail(guestEmail);
        reservationRepository.save(reservation);
        log.info("Reservation updated id={}, traceId={}", reservationId, traceId);
        eventPublisher.publish(new ReservationUpdated(reservationId, traceId));
    }

    @Transactional
    public void cancelReservation(UUID reservationId, String traceId) {
        log.info("Cancelling reservation id={}, traceId={}", reservationId, traceId);

        Optional<Reservation> optReservation = reservationRepository.findById(reservationId);
        if (optReservation.isEmpty()) {
            log.warn("Reservation not found id={}, traceId={}", reservationId, traceId);
            eventPublisher.publish(new ReservationFailed(reservationId,
                    "Reservation not found", traceId));
            return;
        }
        Reservation reservation = optReservation.get();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Reservation cancelled id={}, traceId={}", reservationId, traceId);
        eventPublisher.publish(new ReservationCancelled(reservationId, traceId));
    }
}
