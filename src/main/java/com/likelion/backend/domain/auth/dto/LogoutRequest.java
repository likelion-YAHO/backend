package com.likelion.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그아웃 요청")
public class LogoutRequest {

  @NotBlank(message = "refresh token은 필수입니다.")
  @Schema(description = "무효화할 refresh token")
  private String refreshToken;
}
