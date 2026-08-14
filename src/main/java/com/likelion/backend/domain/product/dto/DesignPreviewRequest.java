package com.likelion.backend.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "디자인 미리보기 요청 (미리보기 버튼 전용)")
public class DesignPreviewRequest {

    @NotNull(message = "시안 ID는 필수입니다.")
  @Schema(description = "선택한 designOptionId", example = "1")
  private Long designOptionId;

  @Schema(description = "포인트 컬러 코드 (선택)", example = "PINK")
  private String pointColor;

  @Schema(description = "메탈 컬러 코드 (선택)", example = "GOLD")
  private String metalColor;

  @Schema(description = "레더 참/키링 추가상품 ID (선택)", example = "3")
  private Long charmOptionId;

  @Schema(description = "스카프 추가상품 ID (선택)", example = "7")
  private Long scarfOptionId;
}
