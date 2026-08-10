package com.likelion.backend.domain.review.entity;

import com.likelion.backend.domain.product.entity.ProductCategory;
import com.likelion.backend.domain.reservation.entity.Reservation;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** unique, 예약당 리뷰 1개 */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", nullable = false, unique = true)
  private Reservation reservation;

  /** 최소 20자 이상 텍스트 */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /** 작성 시점 제품 카테고리 스냅샷 */
  @Column(nullable = false, length = 50)
  private ProductCategory category;
}
