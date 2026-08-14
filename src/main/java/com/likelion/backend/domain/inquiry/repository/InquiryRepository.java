package com.likelion.backend.domain.inquiry.repository;

import com.likelion.backend.domain.inquiry.entity.Inquiry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

  List<Inquiry> findAllByUserId(Long userId);

  List<Inquiry> findAllByReservationId(Long reservationId);
}
