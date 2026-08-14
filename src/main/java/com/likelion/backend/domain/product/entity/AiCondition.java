package com.likelion.backend.domain.product.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI가 판정한 제품 상태. DB에는 {@link #label} 값이 그대로 저장된다(AiConditionConverter 참고)
 */
@Getter
@RequiredArgsConstructor
public enum AiCondition {
  HIGH("상"),
  MEDIUM("중"),
  LOW("하");

  private final String label;

  public static AiCondition fromLabel(String label) {
    return Arrays.stream(values())
        .filter(condition -> condition.label.equals(label))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상태 값입니다: " + label));
  }
}
