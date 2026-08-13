package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false")
public class MockProductDesignRecommender implements ProductDesignRecommender {

  @Override
  public List<DesignRecommendation> recommend(
      Product product, List<ProductImage> images, String userPrompt) {
    String parts =
        product.getRecyclableParts() != null
            ? product.getRecyclableParts()
            : "원본 가죽/원단";
    String sizeHint =
        product.getSizeHint() != null ? product.getSizeHint() : "원본 크기에 맞는 시안";
    String previewUrl =
        (images != null && !images.isEmpty()) ? images.get(0).getImageUrl() : null;

    // 테스트 2개
    return List.of(
        DesignRecommendation.builder()
            .name("소형 키링형")
            .description("[소형] " + parts + " 조각 활용. 가이드: " + sizeHint + ". 요청: " + userPrompt)
            .imageUrl(previewUrl)
            .build(),
        DesignRecommendation.builder()
            .name("중형 파우치형")
            .description("[중형] " + parts + " 패널 활용. 가이드: " + sizeHint + ". 요청: " + userPrompt)
            .imageUrl(previewUrl)
            .build());
  }
}
