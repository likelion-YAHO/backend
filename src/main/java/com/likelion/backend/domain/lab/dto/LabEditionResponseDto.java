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

    @Schema(description = "콘셉트/시즌명", example = "Summer Remix")
    private String concept;

    @Schema(description = "이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "창작자 닉네임", example = "김사자")
    private String nickname;

    @Schema(description = "실물 제작 상태", example = "READY")
    private ProductionStatus productionStatus;

    @Schema(description = "판매 가격", example = "1500000")
    private Integer price;

    @Schema(description = "품절 여부", example = "true")
    private Boolean isSoldOut;

    @Schema(description = "색상", example = "Sand")
    private String color;

    @Schema(description = "사이즈", example = "M")
    private String size;

    @Schema(description = "제품 상세 정보", example = "피라미드 모양 스터드 장식과 천연 나파 가죽 트림이 특징인 비세토스 모노그램 캔버스 백팩")
    private String description;

    @Schema(description = "온라인 남은 재고 수량", example = "2")
    private Integer stock;

    @Schema(description = "컬러 칩 Hex 코드", example = "#E1C699")
    private String colorHex;

    public LabEditionResponseDto(LabDesign design) {
        this.id = design.getId();
        this.designName = design.getDesignName();
        this.concept = design.getConcept();
        this.imageUrl = design.getImageUrl();
        this.nickname = (design.getUser() != null) ? design.getUser().getNickname() : "알 수 없음";
        this.productionStatus = design.getProductionStatus();
        this.price = design.getPrice();
        this.color = design.getColor();
        this.size = design.getSize();
        this.description = design.getDescription();
        this.stock = design.getStock();
        this.isSoldOut = (design.getStock() == null || design.getStock() <= 0);
    }
}