package com.likelion.backend.domain.inquiry.service;

import com.likelion.backend.domain.inquiry.dto.InquiryRequestDto;
import com.likelion.backend.domain.inquiry.entity.Inquiry;
import com.likelion.backend.domain.inquiry.repository.InquiryRepository;
import com.likelion.backend.domain.reservation.entity.Reservation;
import com.likelion.backend.domain.reservation.repository.ReservationRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public void createInquiry(Long userId, String orderNumber, InquiryRequestDto request) {
        // 1. 유저가 없으면 팀 공통 에러코드 A005 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        // 2. 예약이 없으면 팀 공통 에러코드 R001 반환
        Reservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESERVATION_NOT_FOUND));

        // 3. 문의 엔티티 생성 및 저장
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .reservation(reservation)
                .content(request.getContent())
                .build();

        inquiryRepository.save(inquiry);
    }
}