package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiDesignRequestDto {

    @Schema(description = "커스텀할 베이스 제품", example = "STARK_SIDE_STUDS_BACKPACK")
    private BaseProduct baseProduct;

    @Schema(description = "사용자가 입력한 AI 디자인 가이드(프롬프트)", example = "포켓의 가죽을 비세토스 스웨이드 꼬냑으로 변경하고 메탈 컬러를 은색으로 바꿔주세요.")
    private String prompt;
}