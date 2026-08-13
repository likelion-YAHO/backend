package com.likelion.backend.domain.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiDesignResponseDto {

    @Schema(description = "AI가 생성한 시안 이미지 URL 리스트 (최대 3개)")
    private List<String> generatedImages;
}