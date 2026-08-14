package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.LabDesign;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LabDesignListResponseDto {

    @Schema(description = "출품작 ID", example = "1")
    private Long id;

    @Schema(description = "디자인명", example = "나만의 꼬냑 백팩")
    private String designName;

    @Schema(description = "썸네일 이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "좋아요 수", example = "42")
    private Integer likesCount;

    @Schema(description = "창작자 닉네임", example = "김사자")
    private String nickname;

    public LabDesignListResponseDto(LabDesign design) {
        this.id = design.getId();
        this.designName = design.getDesignName();
        this.imageUrl = design.getImageUrl();
        this.likesCount = design.getLikesCount();
        this.nickname = design.getUser().getNickname();
    }
}