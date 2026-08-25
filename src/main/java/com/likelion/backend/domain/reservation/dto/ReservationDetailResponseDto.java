package com.likelion.backend.domain.reservation.dto;

import com.likelion.backend.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationDetailResponseDto {
    private Long reservationId;
    private String orderNumber;
    private String productName;
    private String storeName;
    private String currentStatus;
    private LocalDateTime visitDate;
    private String barcode;

    private Long reformId;
    private Long storeId;
    private String productImageUrl;

    private LocalDateTime receivedAt;
    private LocalDateTime consultingAt;
    private LocalDateTime hqArrivedAt;
    private LocalDateTime inspectingAt;
    private LocalDateTime inProgressAt;
    private LocalDateTime completedAt;
    private LocalDateTime shippingAt;
    private LocalDateTime storeArrivedAt;
    private LocalDateTime estimatedStoreArrivalDate;
    private LocalDateTime pickedUpAt;

    public static ReservationDetailResponseDto from(Reservation reservation) {
        return ReservationDetailResponseDto.builder()
                .reservationId(reservation.getId())
                .orderNumber(reservation.getOrderNumber())
                .productName(reservation.getReform().getTargetItem())
                .storeName(reservation.getStore().getName())
                .currentStatus(reservation.getStatus().getLabel())
                .visitDate(reservation.getVisitDate())
                .barcode(reservation.getBarcode())
                .reformId(reservation.getReform().getId())
                .storeId(reservation.getStore().getId())
                .productImageUrl(reservation.getReform().getPreviewImageUrl())
                .receivedAt(reservation.getReceivedAt())
                .consultingAt(reservation.getConsultingAt())
                .hqArrivedAt(reservation.getHqArrivedAt())
                .inspectingAt(reservation.getInspectingAt())
                .inProgressAt(reservation.getInProgressAt())
                .completedAt(reservation.getCompletedAt())
                .shippingAt(reservation.getShippingAt())
                .storeArrivedAt(reservation.getStoreArrivedAt())
                .estimatedStoreArrivalDate(reservation.getEstimatedStoreArrivalDate())
                .pickedUpAt(reservation.getPickedUpAt())
                .build();
    }
}