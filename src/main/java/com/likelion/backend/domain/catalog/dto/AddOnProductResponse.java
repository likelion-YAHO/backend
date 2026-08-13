package com.likelion.backend.domain.catalog.dto;

import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "추가상품")
public class AddOnProductResponse {

  @Schema(description = "추가상품 ID", example = "1")
  private Long id;

  @Schema(description = "이름", example = "비셔스 도그 참")
  private String name;

  @Schema(description = "카테고리 코드", example = "KEYRING")
  private String category;

  @Schema(description = "카테고리 한글", example = "레더 참 & 키링")
  private String categoryLabel;

  @Schema(description = "가격", example = "150000")
  private int price;

  @Schema(description = "이미지 URL")
  private String imageUrl;

  public static AddOnProductResponse from(AddOnProduct product) {
    return AddOnProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .category(product.getCategory().name())
        .categoryLabel(product.getCategory().getLabel())
        .price(product.getPrice())
        .imageUrl(product.getImageUrl())
        .build();
  }
}
