package com.likelion.backend.domain.product.dto;

import com.likelion.backend.domain.product.entity.DesignOption;
import com.likelion.backend.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "디자인 시안 추천 응답")
public class DesignAnalysisResponse {

  @Schema(description = "제품 ID", example = "12")
  private Long productId;

  @Schema(description = "저장된 사용자 프롬프트")
  private String userPrompt;

  @Schema(description = "AI 상태 코드", example = "HIGH")
  private String aiCondition;

  @Schema(description = "AI 상태 한글", example = "상")
  private String aiConditionLabel;

  @Schema(description = "재활용 가능 부위 (시안 입력에 사용됨)")
  private String recyclableParts;

  @Schema(description = "권장 시안 규모 힌트 (상태+제품 종류/크기)")
  private String sizeHint;

  @Schema(description = "추천 시안 목록")
  private List<DesignOptionResponse> designOptions;

  public static DesignAnalysisResponse of(
      Product product, List<DesignOption> options) {
    return DesignAnalysisResponse.builder()
        .productId(product.getId())
        .userPrompt(product.getUserPrompt())
        .aiCondition(product.getAiCondition().name())
        .aiConditionLabel(product.getAiCondition().getLabel())
        .recyclableParts(product.getRecyclableParts())
        .sizeHint(product.getSizeHint())
        .designOptions(options.stream().map(DesignOptionResponse::from).toList())
        .build();
  }
}

