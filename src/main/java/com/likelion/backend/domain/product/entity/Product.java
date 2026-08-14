package com.likelion.backend.domain.product.entity;

import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 50)
  private ProductCategory category;

  /** AI가 판정한 제품 상태 */
  @Column(name = "ai_condition", nullable = false, length = 10)
  private AiCondition aiCondition;

  /** AI가 판정한 업사이클링 가능 여부 */
  @Column(name = "is_upcyclable", nullable = false)
  private boolean upcyclable;

  /** 원본 제품에서 재활용 가능한 부위/소재 설명 (시안 AI 입력용) */
  @Column(name = "recyclable_parts", length = 1000)
  private String recyclableParts;

  /**
   * 상태 + 원본 제품 종류/크기 기준 권장 시안 규모 힌트
   */
  @Column(name = "size_hint", length = 500)
  private String sizeHint;

  /** AI 생성 시 유저가 추가 입력한 텍스트 */
  @Column(name = "user_prompt", length = 255)
  private String userPrompt;

  /** product 단위 AI 추천 키링/참 (add_on_products.id) */
  @Column(name = "recommended_charm_id")
  private Long recommendedCharmId;

  @Column(name = "recommended_charm_name", length = 100)
  private String recommendedCharmName;

  /** product 단위 AI 추천 스카프 (add_on_products.id) */
  @Column(name = "recommended_scarf_id")
  private Long recommendedScarfId;

  @Column(name = "recommended_scarf_name", length = 100)
  private String recommendedScarfName;

  public void updateUserPrompt(String userPrompt) {
    this.userPrompt = userPrompt;
  }

  public void updateAddonRecommendations(
      Long charmId, String charmName, Long scarfId, String scarfName) {
    this.recommendedCharmId = charmId;
    this.recommendedCharmName = charmName;
    this.recommendedScarfId = scarfId;
    this.recommendedScarfName = scarfName;
  }
}
