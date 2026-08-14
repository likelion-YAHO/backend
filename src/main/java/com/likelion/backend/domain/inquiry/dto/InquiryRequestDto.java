package com.likelion.backend.domain.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryRequestDto {

    @NotBlank(message = "문의 내용을 입력해주세요.")
    @Schema(description = "문의 내용", example = "문의드립니다.")
    private String content;
}