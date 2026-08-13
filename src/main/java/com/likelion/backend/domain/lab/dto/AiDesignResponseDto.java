package com.likelion.backend.domain.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiDesignResponseDto {

    @Schema(description = "생성된 AI 디자인 시안 이미지 URL", example = "https://dummy-image-url.com/mcm-bag.jpg")
    private String imageUrl;
}