package com.likelion.backend.domain.reservation.repository;

import com.likelion.backend.domain.reservation.entity.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  List<Reservation> findAllByUserId(Long userId);

  Optional<Reservation> findByOrderNumber(String orderNumber);
}
