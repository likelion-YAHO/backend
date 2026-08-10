package com.likelion.backend.domain.reservation.dto;

import com.likelion.backend.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponseDto {
    private Long reservationId;
    private String orderNumber;
    private String currentStatus;
    private LocalDateTime visitDate;
    private String barcode;

    // TODO: 추후 Product, Store 엔티티 연동 후 주석 해제하여 매핑
    // private String productName;
    // private String storeName;

    // TODO: 피그마 UI에 맞춘 타임라인 시간 컬럼들이 엔티티에 추가되면 여기에 필드 추가 필요!

    public static ReservationResponseDto from(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .orderNumber(reservation.getOrderNumber())
                .currentStatus(reservation.getStatus().getLabel()) // Enum의 "접수 완료" 같은 label 반환
                .visitDate(reservation.getVisitDate())
                .barcode(reservation.getBarcode())
                .build();
    }
}