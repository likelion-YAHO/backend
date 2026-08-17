package com.likelion.backend.domain.reservation.service;

import com.likelion.backend.domain.reform.entity.Reform;
import com.likelion.backend.domain.reservation.dto.ReservationCreateRequest;
import com.likelion.backend.domain.reservation.dto.ReservationDetailResponseDto;
import com.likelion.backend.domain.reservation.dto.ReservationResponseDto;
import com.likelion.backend.domain.reservation.entity.Reservation;
import com.likelion.backend.domain.reservation.entity.ReservationStatus;
import com.likelion.backend.domain.reservation.repository.ReservationRepository;
import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final com.likelion.backend.domain.user.repository.UserRepository userRepository; // 유저 조회용 (이미 있다면 주입받기)
    private final com.likelion.backend.domain.reform.repository.ReformRepository reformRepository; // 리폼 제품 조회용
    private final com.likelion.backend.domain.store.repository.StoreRepository storeRepository;     // 매장 조회용

    // 1. 내 예약 목록 조회
    public List<ReservationResponseDto> getMyReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        return reservations.stream()
                .map(ReservationResponseDto::from)
                .collect(Collectors.toList());
    }

    // 2. [사용자용] 예약 상세 조회
    public ReservationDetailResponseDto getReservationDetail(Long reservationId, Long userId) {
        return ReservationDetailResponseDto.from(getOwnedReservation(reservationId, userId));
    }

    // 3. [매장 직원용] 바코드 스캔 조회
    public ReservationDetailResponseDto getReservationByOrderNumber(String orderNumber) {
        Reservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESERVATION_NOT_FOUND));

        return ReservationDetailResponseDto.from(reservation);
    }

    @Transactional
    public ReservationDetailResponseDto createReservation(Long userId, ReservationCreateRequest request) {
        // 1. 유저, 제품, 매장 엔티티 조회 (없으면 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.USER_NOT_FOUND));

        Reform reform = getOwnedReform(request.getReformId(), userId);
        if (reservationRepository.existsByReform_Id(reform.getId())) {
            throw new CustomException(GlobalErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.STORE_NOT_FOUND));

        // 2. 고유 주문 번호 생성 (예: UPC-7K4D-92LM)
        String orderNumber = generateOrderNumber();

        // 3. 예약 엔티티 생성 (Builder 패턴 활용)
        Reservation reservation = Reservation.builder()
                .user(user)
                .reform(reform)
                .store(store)
                .orderNumber(orderNumber)
                .visitDate(request.getVisitDate())
                .status(ReservationStatus.RECEIVED)
                .receivedAt(LocalDateTime.now()) // 생성 시 '접수 완료' 시간 세팅
                .estimatedStoreArrivalDate(LocalDateTime.now().plusDays(1)) // 예상 도착일을 내일(plusDays(1))로 세팅
                .barcode(orderNumber)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationDetailResponseDto.from(savedReservation);
    }

    // 5. 예약 취소
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = getOwnedReservation(reservationId, userId);

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(GlobalErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        // 상태를 취소로 변경
        reservation.updateStatus(ReservationStatus.CANCELLED);
    }

    @Transactional
    public ReservationDetailResponseDto updateReservation(Long reservationId, ReservationCreateRequest request, Long userId) {
        Reservation reservation = getOwnedReservation(reservationId, userId);

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.STORE_NOT_FOUND));

        Reform reform = getOwnedReform(request.getReformId(), userId);
        if (!reform.getId().equals(reservation.getReform().getId())
                && reservationRepository.existsByReform_Id(reform.getId())) {
            throw new CustomException(GlobalErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        reservation.updateDetails(request.getVisitDate(), store, reform);

        return ReservationDetailResponseDto.from(reservation);
    }

    @Transactional
    public void restoreReservation(Long reservationId, Long userId) {
        Reservation reservation = getOwnedReservation(reservationId, userId);

        // 취소된 상태가 아니라면 복원할 필요 없음
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            reservation.updateStatus(ReservationStatus.RECEIVED); // 다시 접수 상태로
        }
    }

    private Reservation getOwnedReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(GlobalErrorCode.RESERVATION_ACCESS_DENIED);
        }
        return reservation;
    }

    private Reform getOwnedReform(Long reformId, Long userId) {
        Reform reform = reformRepository.findById(reformId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.REFORM_NOT_FOUND));
        if (!reform.getProduct().getUser().getId().equals(userId)) {
            throw new CustomException(GlobalErrorCode.REFORM_ACCESS_DENIED);
        }
        return reform;
    }

    // UPC-7K4D-92LM 형식의 고유 주문 번호 생성기
    public String generateOrderNumber() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        sb.append("UPC-");
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextBoolean() ? digits.charAt(random.nextInt(10)) : alphabet.charAt(random.nextInt(26)));
        }
        sb.append("-");
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextBoolean() ? digits.charAt(random.nextInt(10)) : alphabet.charAt(random.nextInt(26)));
        }

        return sb.toString();
    }

    @Transactional // 👈 DB 상태가 변경되므로 반드시 트랜잭션 어노테이션 확인!
    public ReservationDetailResponseDto scanBarcodeAndUpdateStatus(String orderNumber) {
        // 1. 기존 예약 조회 로직 (CustomException 에러 코드 수정)
        Reservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESERVATION_NOT_FOUND));

        // 2. 수령 완료 처리 로직 추가 (현재 시간 기록)
        reservation.completePickUp(LocalDateTime.now());

        // 3. 응답 반환 (from 팩토리 메서드 사용)
        return ReservationDetailResponseDto.from(reservation);
    }
}