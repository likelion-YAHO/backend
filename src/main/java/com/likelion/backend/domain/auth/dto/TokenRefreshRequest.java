package com.likelion.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "access token 재발급 요청")
public class TokenRefreshRequest {

  @NotBlank(message = "refresh token은 필수입니다.")
  @Schema(description = "로그인 시 발급받은 refresh token")
  private String refreshToken;
}
