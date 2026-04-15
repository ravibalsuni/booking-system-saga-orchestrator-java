package com.booking.reservation.repo;

import com.booking.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByGuestEmailOrderByDateAsc(String guestEmail);

    boolean existsByDateAndTime(LocalDate date, LocalTime time);
}