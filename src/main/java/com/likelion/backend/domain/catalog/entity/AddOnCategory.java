package com.likelion.backend.domain.catalog.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
  public enum AddOnCategory {
    KEYRING("레더 참 & 키링"),
    SCARF("스카프");

    private final String label;

    public static AddOnCategory fromValue(String value) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException("카테고리 값이 비어 있습니다.");
      }
      String trimmed = value.trim();
      return Arrays.stream(values())
          .filter(c -> c.name().equalsIgnoreCase(trimmed) || c.label.equals(trimmed))
          .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 추가상품 카테고리: " + value));
  }
}
