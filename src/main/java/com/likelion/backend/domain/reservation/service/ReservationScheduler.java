package com.likelion.backend.domain.reservation.service;

import com.likelion.backend.domain.reservation.entity.Reservation;
import com.likelion.backend.domain.reservation.entity.ReservationStatus;
import com.likelion.backend.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    // 60000ms = 1분마다 실행
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoAdvanceReservationStatus() {
        log.info("⏰ 1분 스케줄러 작동: 예약 상태를 다음 단계로 자동 업데이트합니다.");

        List<Reservation> reservations = reservationRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation reservation : reservations) {
            ReservationStatus current = reservation.getStatus();

            // 이미 매장에 도착했거나 취소된 예약은 업데이트 제외
            if (current == ReservationStatus.ARRIVED_AT_STORE || current == ReservationStatus.CANCELLED) {
                continue;
            }

            ReservationStatus nextStatus = switch (current) {
                case RECEIVED -> ReservationStatus.ARRIVED_AT_HQ;
                case ARRIVED_AT_HQ -> ReservationStatus.INSPECTING;
                case INSPECTING -> ReservationStatus.IN_PROGRESS;
                case IN_PROGRESS -> ReservationStatus.COMPLETED;
                case COMPLETED -> ReservationStatus.SHIPPING;
                case SHIPPING -> ReservationStatus.ARRIVED_AT_STORE;
                default -> current;
            };

            // 상태 및 시간 업데이트
            reservation.advanceStatus(nextStatus, now);
            log.info("예약 [{}] 상태 변경: {} -> {}", reservation.getId(), current.getLabel(), nextStatus.getLabel());
        }
    }
}