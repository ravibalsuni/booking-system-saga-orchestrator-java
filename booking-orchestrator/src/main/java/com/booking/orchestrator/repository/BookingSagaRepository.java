package com.booking.orchestrator.repository;

import com.booking.orchestrator.entity.BookingSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingSagaRepository extends JpaRepository<BookingSaga, UUID> {

    Optional<BookingSaga> findBySagaId(String sagaId);

    List<BookingSaga> findByGuestEmailOrderByDateAsc(String guestEmail);

    Optional<BookingSaga> findByReservationId(UUID reservationId);
}