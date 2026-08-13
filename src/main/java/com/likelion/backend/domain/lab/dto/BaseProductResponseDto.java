package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class BaseProductResponseDto {

    @Schema(description = "제품 코드 (ENUM)", example = "STARK_SIDE_STUDS_BACKPACK")
    private String code;

    @Schema(description = "화면 표시용 제품명", example = "Stark Side Studs Backpack")
    private String name;

    public BaseProductResponseDto(BaseProduct baseProduct) {
        this.code = baseProduct.name();
        this.name = baseProduct.getProductName();
    }
}