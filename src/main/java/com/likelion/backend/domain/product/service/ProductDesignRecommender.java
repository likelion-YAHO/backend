package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import java.util.List;

/**
 * 사용자 프롬프트 + 제품 이미지 기반 리폼 시안 추천 포트.
 */
public interface ProductDesignRecommender {

  List<DesignRecommendation> recommend(
      Product product, List<ProductImage> images, String userPrompt);
}
