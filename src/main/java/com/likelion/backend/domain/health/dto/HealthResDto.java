package com.likelion.backend.domain.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "헬스체크 응답")
public class HealthResDto {

  @Schema(description = "서버 상태", example = "ok")
  private String status;

  public static HealthResDto ok() {
    return new HealthResDto("ok");
  }
}
