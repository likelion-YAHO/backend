package com.likelion.backend.domain.product.dto;

import com.likelion.backend.domain.product.entity.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "제품 이미지 응답")
public class ProductImageResponse {

  @Schema(description = "이미지 ID", example = "1")
  private Long id;

  @Schema(description = "이미지 URL", example = "http://localhost:8080/uploads/products/uuid.jpg")
  private String imageUrl;

  @Schema(description = "노출 순서", example = "0")
  private int sortOrder;

  public static ProductImageResponse from(ProductImage image) {
    return ProductImageResponse.builder()
        .id(image.getId())
        .imageUrl(image.getImageUrl())
        .sortOrder(image.getSortOrder())
        .build();
  }
}
