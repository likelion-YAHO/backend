package com.likelion.backend.domain.user.dto;

import com.likelion.backend.domain.user.entity.Provider;
import com.likelion.backend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "내 프로필 응답")
public class UserMeResponse {

  @Schema(description = "사용자 ID", example = "1")
  private Long id;

  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @Schema(description = "닉네임", example = "김멋사")
  private String nickname;

  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phone;

  @Schema(description = "프로필 이미지 URL")
  private String profileImage;

  @Schema(description = "가입 경로", example = "LOCAL")
  private Provider provider;

  @Schema(description = "알림 수신 여부", example = "true")
  private boolean alarmEnabled;

  public static UserMeResponse from(User user) {
    return UserMeResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .phone(user.getPhone())
        .profileImage(user.getProfileImage())
        .provider(user.getProvider())
        .alarmEnabled(user.isAlarmEnabled())
        .build();
  }
}
