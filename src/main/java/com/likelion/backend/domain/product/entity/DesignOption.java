package com.likelion.backend.domain.product.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "design_options")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DesignOption extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  /** 시안 이름 (예: 키링형, 토트백형) */
  @Column(nullable = false, length = 100)
  private String name;

  /** 시안 설명 */
  @Column(length = 500)
  private String description;

  /** 시안 미리보기 이미지 URL (AI 생성 이미지 또는 폴백 원본) */
  @Column(name = "image_url", length = 512)
  private String imageUrl;

  @Builder.Default
  @Column(name = "sort_order", nullable = false)
  private int sortOrder = 0;
}
