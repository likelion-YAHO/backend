package com.likelion.backend.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "리폼 ID는 필수입니다.")
    @Schema(description = "리폼 식별자", example = "1")
    private Long reformId;

    @NotNull(message = "매장 ID는 필수입니다.")
    @Schema(description = "매장 식별자", example = "1")
    private Long storeId;

    @NotNull(message = "방문 날짜와 시간은 필수입니다.")
    @Schema(description = "방문 예정 일시", example = "2026-08-18T14:00:00")
    private LocalDateTime visitDate;
}