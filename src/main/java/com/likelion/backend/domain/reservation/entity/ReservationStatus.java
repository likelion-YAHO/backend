package com.likelion.backend.domain.reservation.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예약 진행 상태. DB에는 {@link #label} 값이 그대로 저장된다(ReservationStatusConverter 참고)
 */
@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
  RECEIVED("접수 완료"),
  ARRIVED_AT_HQ("본사 도착"),
  INSPECTING("제품 검수"),
  IN_PROGRESS("제작 진행"),
  COMPLETED("제작 완료"),
  SHIPPING("배송 중"),
  ARRIVED_AT_STORE("매장 도착"),
  PICKED_UP("수령 완료"),
  CANCELLED("취소");

  private final String label;

  public static ReservationStatus fromLabel(String label) {
    return Arrays.stream(values())
        .filter(status -> status.label.equals(label))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 상태 값입니다: " + label));
  }
}
