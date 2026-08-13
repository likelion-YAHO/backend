package com.likelion.backend.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "디자인 시안 추천 요청")
public class DesignAnalysisRequest {

  @NotBlank(message = "디자인 가이드 프롬프트는 필수입니다.")
  @Size(min=10, message = "프롬프트는 10자 이상여야 합니다.")
  @Schema(description = "AI 디자인 가이드 프롬프트", example = "귀엽고 세련된 동물 키링으로 만들고 싶어요.")
  private String userPrompt;
}
