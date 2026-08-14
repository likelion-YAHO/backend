package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LabEditionResponseDto {

    @Schema(description = "출품작 ID", example = "1")
    private Long id;

    @Schema(description = "디자인명", example = "나만의 꼬냑 백팩")
    private String designName;

    @Schema(description = "이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "창작자 닉네임", example = "김사자")
    private String nickname;

    @Schema(description = "실물 제작 상태", example = "READY")
    private ProductionStatus productionStatus;

    @Schema(description = "판매 가격", example = "1500000")
    private Integer price;

    public LabEditionResponseDto(LabDesign design) {
        this.id = design.getId();
        this.designName = design.getDesignName();
        this.imageUrl = design.getImageUrl();
        this.nickname = design.getUser().getNickname();
        this.productionStatus = design.getProductionStatus();
        this.price = design.getPrice();
    }
}