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

    @Schema(description = "베이스 제품 코드", example = "STARK_SIDE_STUDS_BACKPACK")
    private String baseProduct;

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

    @Schema(description = "출품 시 선택한 포인트 컬러", example = "PINK")
    private String pointColor;

    @Schema(description = "출품 시 선택한 메탈 컬러", example = "GOLD")
    private String metalColor;

    @Schema(description = "출품 시 선택한 키링/참 ID", example = "3")
    private Long charmOptionId;

    @Schema(description = "출품 시 선택한 키링/참 이름", example = "베어 키링")
    private String charmOptionName;

    @Schema(description = "출품 시 선택한 스카프 ID", example = "5")
    private Long scarfOptionId;

    @Schema(description = "출품 시 선택한 스카프 이름", example = "레드 모노그램 스카프")
    private String scarfOptionName;

    @Schema(description = "시안 생성 당시 AI 추천 키링/참 ID", example = "3")
    private Long recommendedCharmId;

    @Schema(description = "시안 생성 당시 AI 추천 키링/참 이름", example = "베어 키링")
    private String recommendedCharmName;

    @Schema(description = "시안 생성 당시 AI 추천 스카프 ID", example = "5")
    private Long recommendedScarfId;

    @Schema(description = "시안 생성 당시 AI 추천 스카프 이름", example = "레드 모노그램 스카프")
    private String recommendedScarfName;

    public LabDesignDetailResponseDto(
            LabDesign design, String charmOptionName, String scarfOptionName) {
        this.id = design.getId();
        this.designName = design.getDesignName();
        this.baseProduct = design.getBaseProduct() == null ? null : design.getBaseProduct().name();
        this.baseProductName = design.getBaseProduct() == null ? null : design.getBaseProduct().getProductName();
        this.concept = design.getConcept();
        this.aiPrompt = design.getAiPrompt();
        this.usedMaterials = design.getUsedMaterials();
        this.imageUrl = design.getImageUrl();
        this.likesCount = design.getLikesCount();
        this.nickname = design.getUser().getNickname();
        this.isOfficialSelection = design.getIsOfficialSelection();
        this.productionStatus = design.getProductionStatus();
        this.price = design.getPrice();
        this.pointColor = design.getPointColor();
        this.metalColor = design.getMetalColor();
        this.charmOptionId = design.getCharmOptionId();
        this.charmOptionName = charmOptionName;
        this.scarfOptionId = design.getScarfOptionId();
        this.scarfOptionName = scarfOptionName;
        this.recommendedCharmId = design.getRecommendedCharmId();
        this.recommendedCharmName = design.getRecommendedCharmName();
        this.recommendedScarfId = design.getRecommendedScarfId();
        this.recommendedScarfName = design.getRecommendedScarfName();
    }
}
