package com.booking.confirmation.repo;

import com.booking.confirmation.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, UUID> {

    Optional<Confirmation> findByReservationId(UUID reservationId);
}
