package com.likelion.backend.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponseDto {

    @Schema(description = "매장 ID", example = "1")
    private Long id;

    @Schema(description = "매장 이름", example = "MCM 롯데백화점 영등포점")
    private String name;

    @Schema(description = "매장 주소", example = "서울특별시 영등포구 경인로 846")
    private String address;

    @Schema(description = "매장 전화번호", example = "02-2638-2000")
    private String phone;

    @Schema(description = "위도", example = "37.5165")
    private Double latitude;

    @Schema(description = "경도", example = "126.9070")
    private Double longitude;

    @Schema(description = "현재 위치로부터의 거리 (km)", example = "1.25")
    private Double distance;
}