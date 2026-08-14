package com.likelion.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "프로필 수정 요청 (보낸 필드만 변경)")
public class UpdateProfileRequest {

  @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
  @Schema(description = "이름/닉네임", example = "김서경")
  private String nickname;

  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
  @Schema(description = "이메일(로그인 ID)", example = "skuteam5@gmail.com")
  private String email;

  @Size(max = 64, message = "전화번호는 64자 이하여야 합니다.")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phone;
}
