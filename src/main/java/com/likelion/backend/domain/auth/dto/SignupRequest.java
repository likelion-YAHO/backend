package com.likelion.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이메일 회원가입 요청")
public class SignupRequest {

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(max = 64, message = "비밀번호는 64자 이하여야 합니다.")
  @Schema(description = "비밀번호", example = "password123")
  private String password;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
  @Schema(description = "닉네임", example = "김멋사")
  private String nickname;

  @Size(max = 64, message = "전화번호는 64자 이하여야 합니다.")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phone;
}
