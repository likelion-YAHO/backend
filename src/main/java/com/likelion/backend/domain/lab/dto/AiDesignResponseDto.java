package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.product.dto.DesignOptionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiDesignResponseDto {

    @Schema(description = "생성된 AI 디자인 시안 이미지 URL")
    private String imageUrl;

    @Schema(description = "이번 커스텀에서 사용한 생성 횟수 (최대 3)", example = "1")
    private int tryCount;

    @Schema(description = "product 단위 AI 추천 키링/참 ID (design-preview charmOptionId)", example = "3")
    private Long recommendedCharmId;

    @Schema(description = "product 단위 AI 추천 키링/참 이름", example = "베어 키링")
    private String recommendedCharmName;

    @Schema(description = "product 단위 AI 추천 스카프 ID (design-preview scarfOptionId)", example = "5")
    private Long recommendedScarfId;

    @Schema(description = "product 단위 AI 추천 스카프 이름", example = "레드 모노그램 스카프")
    private String recommendedScarfName;

    @Schema(description = "추천 시안 목록")
    private List<DesignOptionResponse> designOptions;
}