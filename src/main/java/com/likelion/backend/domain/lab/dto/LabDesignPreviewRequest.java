package com.likelion.backend.domain.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "LAB 커스텀 미리보기 요청 (미리보기 버튼 전용)")
public class LabDesignPreviewRequest {

  @NotBlank(message = "시안 이미지 URL은 필수입니다.")
  @Schema(
      description = "POST /api/lab/designs/generate 가 반환한 시안 이미지 URL",
      example = "http://localhost:8080/uploads/lab-designs/uuid.png")
  private String sourceImageUrl;

  @Schema(description = "포인트 컬러 코드 (선택)", example = "PINK")
  private String pointColor;

  @Schema(description = "메탈 컬러 코드 (선택)", example = "GOLD")
  private String metalColor;

  @Schema(description = "레더 참/키링 추가상품 ID (선택)", example = "3")
  private Long charmOptionId;

  @Schema(description = "스카프 추가상품 ID (선택)", example = "5")
  private Long scarfOptionId;
}
