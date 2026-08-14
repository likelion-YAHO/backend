package com.likelion.backend.domain.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LabDesignLikeResponseDto {

    @Schema(description = "현재 좋아요 상태 (true: 좋아요 누름, false: 좋아요 취소됨)", example = "true")
    private boolean isLiked;

    @Schema(description = "업데이트된 총 좋아요 수", example = "43")
    private int totalLikes;
}