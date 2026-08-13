package com.likelion.backend.domain.product.dto;

import com.likelion.backend.domain.product.entity.DesignOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "리폼 시안 옵션")
public class DesignOptionResponse {

  @Schema(description = "시안 ID", example = "101")
  private Long id;

  @Schema(description = "시안 이름", example = "키링형")
  private String name;

  @Schema(description = "시안 설명", example = "본체를 키링/참 세트로 리폼")
  private String description;

  @Schema(description = "미리보기 이미지 URL")
  private String imageUrl;

  @Schema(description = "노출 순서", example = "0")
  private int sortOrder;

  public static DesignOptionResponse from(DesignOption option) {
    return DesignOptionResponse.builder()
        .id(option.getId())
        .name(option.getName())
        .description(option.getDescription())
        .imageUrl(option.getImageUrl())
        .sortOrder(option.getSortOrder())
        .build();
  }
}
