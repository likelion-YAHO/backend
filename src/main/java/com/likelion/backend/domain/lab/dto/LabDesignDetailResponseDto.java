package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LabDesignDetailResponseDto {

    @Schema(description = "출품작 ID", example = "1")
    private Long id;

    @Schema(description = "디자인명", example = "나만의 꼬냑 백팩")
    private String designName;

    @Schema(description = "베이스 제품명", example = "Stark Side Studs Backpack")
    private String baseProductName;

    @Schema(description = "디자인 콘셉트", example = "빈티지 비세토스를 활용한 에스닉한 느낌")
    private String concept;

    @Schema(description = "AI 디자인 가이드(프롬프트)", example = "포켓의 가죽을 비세토스 스웨이드 꼬냑으로 변경...")
    private String aiPrompt;

    @Schema(description = "사용한 소재", example = "Vintage Visetos, Pink Leather")
    private String usedMaterials;

    @Schema(description = "최종 렌더링 이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "좋아요 수", example = "42")
    private Integer likesCount;

    @Schema(description = "창작자 닉네임", example = "김사자")
    private String nickname;

    @Schema(description = "본사 선정 여부", example = "false")
    private Boolean isOfficialSelection;

    @Schema(description = "실물 제작 진행 상태", example = "VIRTUAL")
    private ProductionStatus productionStatus;

    @Schema(description = "한정판 판매 가격 (미정 시 null)", example = "1500000")
    private Integer price;

    public LabDesignDetailResponseDto(LabDesign design) {
        this.id = design.getId();
        this.designName = design.getDesignName();
        this.baseProductName = design.getBaseProduct().getProductName();
        this.concept = design.getConcept();
        this.aiPrompt = design.getAiPrompt();
        this.usedMaterials = design.getUsedMaterials();
        this.imageUrl = design.getImageUrl();
        this.likesCount = design.getLikesCount();
        this.nickname = design.getUser().getNickname();
        this.isOfficialSelection = design.getIsOfficialSelection();
        this.productionStatus = design.getProductionStatus();
        this.price = design.getPrice();
    }
}