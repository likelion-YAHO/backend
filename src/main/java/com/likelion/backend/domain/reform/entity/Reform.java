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

  /** 선택한 시안 ID */
  @Column(name = "design_option_id")
  private Long designOptionId;

  /** AI 추천 디자인 중 선택한 형태 (예: 키링형) */
  @Column(name = "target_item", nullable = false, length = 100)
  private String targetItem;

  /** 선택 안 하면 null */
  @Column(name = "point_color", length = 50)
  private String pointColor;

  /** 골드, 실버 등. 선택 안 하면 null */
  @Column(name = "metal_color", length = 50)
  private String metalColor;

  /** 참/키링 표시명 스냅샷 */
  @Column(name = "charm_option", length = 100)
  private String charmOption;

  @Column(name = "charm_option_id")
  private Long charmOptionId;

  /** 스카프 표시명 스냅샷 */
  @Column(name = "scarf_option", length = 100)
  private String scarfOption;

  @Column(name = "scarf_option_id")
  private Long scarfOptionId;

  /** 선택 완료 시점 미리보기 이미지 (캐시/시안 URL) */
  @Column(name = "preview_image_url", length = 512)
  private String previewImageUrl;

  /** 기본 리폼/수선비 */
  @Column(name = "repair_fee", nullable = false)
  private int repairFee;

  @Column(name = "total_price", nullable = false)
  private int totalPrice;

  /** 동일 제품 재선택 완료 시 내용 갱신 (reformId 유지) */
  public void replaceSelection(
      Long designOptionId,
      String targetItem,
      String pointColor,
      String metalColor,
      Long charmOptionId,
      String charmOption,
      Long scarfOptionId,
      String scarfOption,
      String previewImageUrl,
      int repairFee,
      int totalPrice) {
    this.designOptionId = designOptionId;
    this.targetItem = targetItem;
    this.pointColor = pointColor;
    this.metalColor = metalColor;
    this.charmOptionId = charmOptionId;
    this.charmOption = charmOption;
    this.scarfOptionId = scarfOptionId;
    this.scarfOption = scarfOption;
    this.previewImageUrl = previewImageUrl;
    this.repairFee = repairFee;
    this.totalPrice = totalPrice;
  }
}
