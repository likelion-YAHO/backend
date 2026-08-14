package com.likelion.backend.domain.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiDesignResponseDto {

    @Schema(description = "생성된 AI 디자인 시안 이미지 URL")
    private String imageUrl;

    @Schema(description = "이번 커스텀에서 사용한 생성 횟수 (최대 3)", example = "1")
    private int tryCount;
}