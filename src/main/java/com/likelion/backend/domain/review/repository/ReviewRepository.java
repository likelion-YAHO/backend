package com.likelion.backend.domain.review.repository;

import com.likelion.backend.domain.review.entity.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  Optional<Review> findByReservationId(Long reservationId);

  boolean existsByReservationId(Long reservationId);

  List<Review> findAllByUserId(Long userId);
}
