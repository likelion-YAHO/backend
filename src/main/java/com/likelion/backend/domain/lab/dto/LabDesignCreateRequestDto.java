package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LabDesignCreateRequestDto {

    @Schema(description = "참여할 미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "베이스 제품", example = "STARK_SIDE_STUDS_BACKPACK")
    private BaseProduct baseProduct;

    @Schema(description = "디자인명", example = "나만의 꼬냑 백팩")
    private String designName;

    @Schema(description = "디자인 콘셉트", example = "빈티지 비세토스를 활용한 에스닉한 느낌")
    private String concept;

    @Schema(description = "AI 디자인 가이드(프롬프트)", example = "포켓의 가죽을 비세토스 스웨이드 꼬냑으로 변경...")
    private String aiPrompt;

    @Schema(description = "사용한 소재", example = "Vintage Visetos, Pink Leather")
    private String usedMaterials;

    @Schema(description = "최종 생성된 이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "포인트 컬러 코드 (선택)", example = "PINK")
    private String pointColor;

    @Schema(description = "메탈 컬러 코드 (선택)", example = "GOLD")
    private String metalColor;

    @Schema(description = "추가 레더 참/키링 ID (선택)", example = "3")
    private Long charmOptionId;

    @Schema(description = "추가 스카프 ID (선택)", example = "5")
    private Long scarfOptionId;
}
