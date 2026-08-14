package com.likelion.backend.domain.product.service;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * AI 시안 추천 결과.
 * 시안 목록 + product 단위 추가상품 추천(키링 1 + 스카프 1).
 */
@Getter
@Builder
public class DesignRecommendResult {

  private final List<DesignRecommendation> designs;

  /** product 단위 추천 키링/참 (카탈로그 id). 없으면 null */
  private final Long recommendedCharmId;

  private final String recommendedCharmName;

  /** product 단위 추천 스카프 (카탈로그 id). 없으면 null */
  private final Long recommendedScarfId;

  private final String recommendedScarfName;
}
