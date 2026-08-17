package com.likelion.backend.domain.reservation.entity;

import com.likelion.backend.domain.reform.entity.Reform;
import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "reservations")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * Reform과 1:1
   */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reform_id", nullable = false, unique = true)
  private Reform reform;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  /**
   * unique, 예: UPC-7K4D-92LM
   */
  @Column(name = "order_number", nullable = false, length = 50, unique = true)
  private String orderNumber;

  /**
   * 2026. 7. 18 - 14:00
   */
  @Column(name = "visit_date", nullable = false)
  private LocalDateTime visitDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReservationStatus status;

  /**
   * 매장 수령용 바코드
   */
  @Column(length = 255)
  private String barcode;

  // 1. 예약 상태 변경 메서드
  public void updateStatus(ReservationStatus status) {
    this.status = status;
  }

  // 2. 예약 상세 정보(방문일시, 매장) 수정 메서드
  public void updateDetails(LocalDateTime visitDate, Store store, Reform reform) {
    this.visitDate = visitDate;
    this.store = store;
    this.reform = reform;
  }

  private LocalDateTime receivedAt;             // 접수 완료 시간
  private LocalDateTime hqArrivedAt;            // 본사 도착 시간
  private LocalDateTime inspectingAt;           // 제품 검수 시간
  private LocalDateTime inProgressAt;           // 제작 진행 시간
  private LocalDateTime completedAt;            // 제작 완료 시간
  private LocalDateTime shippingAt;             // 배송 중 시간
  private LocalDateTime storeArrivedAt;         // 매장 도착 시간
  private LocalDateTime pickedUpAt;

  // 예상 도착일
  private LocalDateTime estimatedStoreArrivalDate;

  // 상태를 다음 단계로 넘기고 시간을 기록하는 편의 메서드
  public void advanceStatus(ReservationStatus nextStatus, LocalDateTime now) {
    this.status = nextStatus;
    switch (nextStatus) {
      case ARRIVED_AT_HQ -> this.hqArrivedAt = now;
      case INSPECTING -> this.inspectingAt = now;
      case IN_PROGRESS -> this.inProgressAt = now;
      case COMPLETED -> this.completedAt = now;
      case SHIPPING -> this.shippingAt = now;
      case ARRIVED_AT_STORE -> this.storeArrivedAt = now;
      default -> {
      }
    }
  }

  public void completePickUp(LocalDateTime now) {
    this.status = ReservationStatus.PICKED_UP;
    this.pickedUpAt = now;
  }
}