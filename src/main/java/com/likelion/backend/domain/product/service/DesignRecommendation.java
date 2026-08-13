package com.likelion.backend.domain.product.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DesignRecommendation {

  private final String name;
  private final String description;

  /** 이미지 생성용 영문/상세 프롬프트 (선택) */
  private final String imagePrompt;

  /** 생성된 시안 이미지 URL. 없으면 서비스에서 제품 원본으로 폴백 */
  private final String imageUrl;
}
