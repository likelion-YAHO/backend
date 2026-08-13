package com.likelion.backend.domain.reform.dto;

import com.likelion.backend.domain.reform.entity.Reform;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "리폼 선택 완료 응답")
public class ReformResponse {

  @Schema(description = "리폼 ID", example = "33")
  private Long reformId;

  @Schema(description = "제품 ID", example = "12")
  private Long productId;

  @Schema(description = "시안 ID")
  private Long designOptionId;

  @Schema(description = "시안/형태 이름", example = "키링형")
  private String targetItem;

  @Schema(description = "포인트 컬러 코드")
  private String pointColor;

  @Schema(description = "메탈 컬러 코드")
  private String metalColor;

  @Schema(description = "참/키링 ID")
  private Long charmOptionId;

  @Schema(description = "참/키링 이름")
  private String charmOption;

  @Schema(description = "스카프 ID")
  private Long scarfOptionId;

  @Schema(description = "스카프 이름")
  private String scarfOption;

  @Schema(description = "미리보기 이미지 URL")
  private String previewImageUrl;

  @Schema(description = "기본 수선/리폼비")
  private int repairFee;

  @Schema(description = "총 금액")
  private int totalPrice;

  @Schema(description = "견적 상세 라인")
  private List<PriceLineResponse> priceBreakdown;

  @Schema(description = "생성 시각")
  private LocalDateTime createdAt;

  public static ReformResponse of(Reform reform, List<PriceLineResponse> lines) {
    return ReformResponse.builder()
        .reformId(reform.getId())
        .productId(reform.getProduct().getId())
        .designOptionId(reform.getDesignOptionId())
        .targetItem(reform.getTargetItem())
        .pointColor(reform.getPointColor())
        .metalColor(reform.getMetalColor())
        .charmOptionId(reform.getCharmOptionId())
        .charmOption(reform.getCharmOption())
        .scarfOptionId(reform.getScarfOptionId())
        .scarfOption(reform.getScarfOption())
        .previewImageUrl(reform.getPreviewImageUrl())
        .repairFee(reform.getRepairFee())
        .totalPrice(reform.getTotalPrice())
        .priceBreakdown(lines)
        .createdAt(reform.getCreatedAt())
        .build();
  }
}
