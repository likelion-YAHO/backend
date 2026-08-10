package com.likelion.backend.domain.reform.entity;

import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reforms")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reform extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  /** AI 추천 디자인 중 선택한 형태 (예: 키링형) */
  @Column(name = "target_item", nullable = false, length = 50)
  private String targetItem;

  @Column(name = "point_color", nullable = false, length = 50)
  private String pointColor;

  /** 골드, 실버 등 */
  @Column(name = "metal_color", nullable = false, length = 50)
  private String metalColor;

  @Column(name = "charm_option", length = 50)
  private String charmOption;

  @Column(name = "scarf_option", length = 50)
  private String scarfOption;

  @Column(name = "total_price", nullable = false)
  private int totalPrice;
}
