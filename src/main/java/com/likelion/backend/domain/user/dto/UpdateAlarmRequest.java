package com.likelion.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "알림 수신 여부 변경 요청")
public class UpdateAlarmRequest {

  @NotNull(message = "알림 수신 여부는 필수입니다.")
  @Schema(description = "알림 수신 여부 (true=수신, false=끄기)", example = "false")
  private Boolean alarmEnabled;
}
