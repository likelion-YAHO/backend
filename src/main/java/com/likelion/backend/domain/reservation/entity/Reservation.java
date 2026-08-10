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

  /** Reform과 1:1 */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reform_id", nullable = false, unique = true)
  private Reform reform;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  /** unique, 예: UPC-7K4D-92LM */
  @Column(name = "order_number", nullable = false, length = 50, unique = true)
  private String orderNumber;

  /** 2026. 7. 18 - 14:00 */
  @Column(name = "visit_date", nullable = false)
  private LocalDateTime visitDate;

  @Column(nullable = false, length = 20)
  private ReservationStatus status;

  /** 매장 수령용 바코드 */
  @Column(length = 255)
  private String barcode;
}
