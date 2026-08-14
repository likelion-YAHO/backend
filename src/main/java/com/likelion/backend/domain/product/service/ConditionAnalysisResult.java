package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.AiCondition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConditionAnalysisResult {

  private final AiCondition condition;
  private final boolean upcyclable;
  private final String message;
  /** 재활용 가능 부위/소재 설명 */
  private final String recyclableParts;
  /** 상태 + 제품 종류/크기 기준 권장 시안 규모 힌트 */
  private final String sizeHint;
}
