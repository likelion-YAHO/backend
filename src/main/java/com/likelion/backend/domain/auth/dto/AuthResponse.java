package com.likelion.backend.domain.auth.dto;

import com.likelion.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "인증 성공 응답 (access + refresh token)")
public class AuthResponse {

  @Schema(description = "JWT access token")
  private String accessToken;

  @Schema(description = "JWT refresh token")
  private String refreshToken;

  @Schema(description = "토큰 타입", example = "Bearer")
  private String tokenType;

  @Schema(description = "access token 만료까지 남은 시간(초)", example = "3600")
  private long expiresIn;

  @Schema(description = "로그인한 사용자 요약 정보")
  private UserSummary user;

  public static AuthResponse of(
      String accessToken, String refreshToken, long expiresIn, User user) {
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(expiresIn)
        .user(UserSummary.from(user))
        .build();
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @Schema(description = "사용자 요약")
  public static class UserSummary {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "김멋사")
    private String nickname;

    public static UserSummary from(User user) {
      return UserSummary.builder()
          .id(user.getId())
          .email(user.getEmail())
          .nickname(user.getNickname())
          .build();
    }
  }
}
