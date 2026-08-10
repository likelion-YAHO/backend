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

  /** AI 생성 시 유저가 추가 입력한 텍스트 */
  @Column(name = "user_prompt", length = 255)
  private String userPrompt;
}
