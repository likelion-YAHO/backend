package com.likelion.backend.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "디자인 미리보기 응답")
public class DesignPreviewResponse {

  @Schema(description = "제품 ID")
  private Long productId;

  @Schema(description = "캐시 히트 여부 (true면 AI 미호출)")
  private boolean cacheHit;

  @Schema(description = "미리보기 이미지 URL")
  private String previewImageUrl;

  @Schema(description = "캐시 키")
  private String cacheKey;

  @Schema(description = "시안 ID")
  private Long designOptionId;

  @Schema(description = "포인트 컬러 코드")
  private String pointColor;

  @Schema(description = "메탈 컬러 코드")
  private String metalColor;

  @Schema(description = "참/키링 ID")
  private Long charmOptionId;

  @Schema(description = "스카프 ID")
  private Long scarfOptionId;

  @Schema(description = "참/키링 이름")
  private String charmOptionName;

  @Schema(description = "스카프 이름")
  private String scarfOptionName;
}
