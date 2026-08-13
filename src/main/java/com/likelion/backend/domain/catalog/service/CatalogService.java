package com.likelion.backend.domain.catalog.service;

import com.likelion.backend.domain.catalog.dto.AddOnProductListResponse;
import com.likelion.backend.domain.catalog.dto.AddOnProductResponse;
import com.likelion.backend.domain.catalog.dto.ColorListResponse;
import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.repository.AddOnProductRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

  private static final List<ColorSwatchResponse> POINT_COLORS =
      List.of(
          swatch("BROWN",   "브라운", "#A33F06"),
          swatch("CORAL", "코랄", "#F16160"),
          swatch("TERRACOTTA", "테라코타", "#E27254"),
          swatch("PINK", "연핑크", "#ECB0CD"),
          swatch("LILAC", "라일락", "#D7B5E8"),
          swatch("PURPLE", "퍼플", "#986BBC"),
          swatch("PLUM", "플럼", "#923D5F"),
          swatch("BLACK", "블랙", "#433A38"),
          swatch("GRAY", "그레이", "#A1A1A1"),
          swatch("WHITE", "화이트", "#F4F4F4"),
          swatch("SKY", "스카이", "#81B8D4"),
          swatch("TEAL", "틸", "#53C0BE"),
          swatch("MINT", "민트", "#B5DBB5"),
          swatch("GREEN", "그린", "#4E824D"));

  private static final List<ColorSwatchResponse> METAL_COLORS =
      List.of(swatch("GOLD", "골드", "#C9A227"), swatch("SILVER", "실버", "#B0BEC5"));

  private static final Map<String, ColorSwatchResponse> POINT_BY_CODE =
      POINT_COLORS.stream()
          .collect(Collectors.toMap(c -> c.getCode().toUpperCase(Locale.ROOT), c -> c));

  private static final Map<String, ColorSwatchResponse> METAL_BY_CODE =
      METAL_COLORS.stream()
          .collect(Collectors.toMap(c -> c.getCode().toUpperCase(Locale.ROOT), c -> c));

  private final AddOnProductRepository addOnProductRepository;

  public ColorListResponse getPointColors() {
    return ColorListResponse.builder().colors(POINT_COLORS).build();
  }

  public ColorListResponse getMetalColors() {
    return ColorListResponse.builder().colors(METAL_COLORS).build();
  }

  public AddOnProductListResponse getAddOnProducts(String categoryValue) {
    List<AddOnProduct> products;
    if (!StringUtils.hasText(categoryValue)) {
      products = addOnProductRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc();
    } else {
      AddOnCategory category;
      try {
        category = AddOnCategory.fromValue(categoryValue);
      } catch (IllegalArgumentException e) {
        throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
      }
      products =
          addOnProductRepository.findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(category);
    }
    return AddOnProductListResponse.builder()
        .items(products.stream().map(AddOnProductResponse::from).toList())
        .build();
  }

  public ColorSwatchResponse requirePointColor(String code) {
    if (!StringUtils.hasText(code)) {
      throw new CustomException(GlobalErrorCode.INVALID_COLOR);
    }
    ColorSwatchResponse color = POINT_BY_CODE.get(code.trim().toUpperCase(Locale.ROOT));
    if (color == null) {
      throw new CustomException(GlobalErrorCode.INVALID_COLOR);
    }
    return color;
  }

  public ColorSwatchResponse requireMetalColor(String code) {
    if (!StringUtils.hasText(code)) {
      throw new CustomException(GlobalErrorCode.INVALID_COLOR);
    }
    ColorSwatchResponse color = METAL_BY_CODE.get(code.trim().toUpperCase(Locale.ROOT));
    if (color == null) {
      throw new CustomException(GlobalErrorCode.INVALID_COLOR);
    }
    return color;
  }

  public AddOnProduct requireActiveAddOn(Long id) {
    return addOnProductRepository
        .findByIdAndActiveTrue(id)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.ADD_ON_PRODUCT_NOT_FOUND));
  }

  private static ColorSwatchResponse swatch(String code, String label, String hex) {
    return ColorSwatchResponse.builder().code(code).label(label).hex(hex).build();
  }
}
