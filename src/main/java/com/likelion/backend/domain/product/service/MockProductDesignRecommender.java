package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.repository.AddOnProductRepository;
import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false")
public class MockProductDesignRecommender implements ProductDesignRecommender {

  private final AddOnProductRepository addOnProductRepository;

  @Override
  public DesignRecommendResult recommend(
      Product product, List<ProductImage> images, String userPrompt) {
    String parts =
        product.getRecyclableParts() != null
            ? product.getRecyclableParts()
            : "원본 가죽/원단";
    String sizeHint =
        product.getSizeHint() != null ? product.getSizeHint() : "원본 크기에 맞는 시안";
    String previewUrl =
        (images != null && !images.isEmpty()) ? images.get(0).getImageUrl() : null;

    String targetForm = detectTargetForm(userPrompt);
    List<DesignRecommendation> designs;
    if (targetForm != null) {
      // 형태 지정: 그 제품만, 스타일 변형
      designs =
          List.of(
              DesignRecommendation.builder()
                  .name(targetForm + " 기본형")
                  .description(parts + " 활용. " + targetForm + " 기본형. 요청: " + userPrompt)
                  .imageUrl(previewUrl)
                  .build(),
              DesignRecommendation.builder()
                  .name(targetForm + " 변형형")
                  .description(parts + " 활용. " + targetForm + " 변형형. 요청: " + userPrompt)
                  .imageUrl(previewUrl)
                  .build());
    } else {
      // 미지정: 기존처럼 크기 스펙트럼
      designs =
          List.of(
              DesignRecommendation.builder()
                  .name("소형 키링형")
                  .description(
                      "[소형] " + parts + " 조각 활용. 가이드: " + sizeHint + ". 요청: " + userPrompt)
                  .imageUrl(previewUrl)
                  .build(),
              DesignRecommendation.builder()
                  .name("중형 파우치형")
                  .description(
                      "[중형] " + parts + " 패널 활용. 가이드: " + sizeHint + ". 요청: " + userPrompt)
                  .imageUrl(previewUrl)
                  .build());
    }

    AddOnProduct charm = firstActive(AddOnCategory.KEYRING);
    AddOnProduct scarf = firstActive(AddOnCategory.SCARF);

    return DesignRecommendResult.builder()
        .designs(designs)
        .recommendedCharmId(charm != null ? charm.getId() : null)
        .recommendedCharmName(charm != null ? charm.getName() : null)
        .recommendedScarfId(scarf != null ? scarf.getId() : null)
        .recommendedScarfName(scarf != null ? scarf.getName() : null)
        .build();
  }

  /** 프롬프트에 특정 제품 형태가 있으면 반환, 없으면 null (기존 스펙트럼 경로) */
  private static String detectTargetForm(String userPrompt) {
    if (!StringUtils.hasText(userPrompt)) {
      return null;
    }
    String p = userPrompt.toLowerCase(Locale.ROOT);
    if (contains(p, "카드지갑", "card wallet", "카드 지갑")) {
      return "카드지갑";
    }
    if (contains(p, "지갑", "wallet")) {
      return "지갑";
    }
    if (contains(p, "파우치", "pouch")) {
      return "파우치";
    }
    if (contains(p, "키링", "keyring", "참")) {
      return "키링";
    }
    if (contains(p, "클러치", "clutch")) {
      return "클러치";
    }
    if (contains(p, "토트", "tote")) {
      return "토트백";
    }
    if (contains(p, "미니백", "mini bag", "미니 백")) {
      return "미니백";
    }
    return null;
  }

  private static boolean contains(String text, String... keys) {
    for (String key : keys) {
      if (text.contains(key.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private AddOnProduct firstActive(AddOnCategory category) {
    List<AddOnProduct> list =
        addOnProductRepository.findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(category);
    return list.isEmpty() ? null : list.get(0);
  }
}
