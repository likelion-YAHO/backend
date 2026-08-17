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
    private String productName; 
    private String storeName;
    private String productImageUrl;

    public static ReservationResponseDto from(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getId())
                .orderNumber(reservation.getOrderNumber())
                .currentStatus(reservation.getStatus().getLabel())
                .visitDate(reservation.getVisitDate())
                .barcode(reservation.getBarcode())
                .productName(reservation.getReform().getTargetItem()) // 매핑
                .storeName(reservation.getStore().getName())         // 매핑
                .build();
    }
}