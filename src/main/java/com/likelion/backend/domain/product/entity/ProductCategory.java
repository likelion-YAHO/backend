package com.likelion.backend.domain.product.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 제품 카테고리. DB에는 {@link #label} 값이 그대로 저장된다(ProductCategoryConverter 참고)
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {
  BACKPACK("백팩"),
  TOTE_SHOULDER("토트백 & 숄더백"),
  SHOULDER_CROSS("숄더백 & 크로스백"),
  MINI_BAG("미니백"),
  CLUTCH_POUCH("클러치 & 파우치"),
  CLOTHING("의류"),
  STRAP_ACCESSORY("스트랩 & 액세서리");

  private final String label;

  public static ProductCategory fromLabel(String label) {
    return Arrays.stream(values())
        .filter(category -> category.label.equals(label))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 값입니다: " + label));
  }

  /** enum name(BACKPACK) 또는 한글 label(백팩) 모두 허용 */
  public static ProductCategory fromValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("카테고리 값이 비어 있습니다.");
    }
    String trimmed = value.trim();
    return Arrays.stream(values())
        .filter(category -> category.name().equalsIgnoreCase(trimmed)
            || category.label.equals(trimmed))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 값입니다: " + value));
  }
}
