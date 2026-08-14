package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import java.util.List;

/**
 * 사용자 프롬프트 + 제품 이미지 기반 리폼 시안 추천 포트
 * 시안 목록과 함께 product 단위 키링/스카프 추천을 반환한다.
 */
public interface ProductDesignRecommender {

  DesignRecommendResult recommend(
      Product product, List<ProductImage> images, String userPrompt);
}
