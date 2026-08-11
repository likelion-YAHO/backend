package com.likelion.backend.domain.auth.controller;

import com.likelion.backend.domain.auth.dto.AuthResponse;
import com.likelion.backend.domain.auth.dto.LoginRequest;
import com.likelion.backend.domain.auth.dto.LogoutRequest;
import com.likelion.backend.domain.auth.dto.SignupRequest;
import com.likelion.backend.domain.auth.dto.TokenRefreshRequest;
import com.likelion.backend.domain.auth.service.AuthService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "회원가입", description = "이메일/비밀번호로 가입하고 access/refresh token을 발급합니다.")
  @PostMapping("/signup")
  public ResponseEntity<BaseResponse<AuthResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    AuthResponse response = authService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "회원가입에 성공했습니다.", response));
  }

  @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 access/refresh token을 발급합니다.")
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(BaseResponse.success("로그인에 성공했습니다.", response));
  }

  @Operation(
      summary = "토큰 재발급",
      description = "refresh token으로 access/refresh token을 재발급합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<BaseResponse<AuthResponse>> refresh(
      @Valid @RequestBody TokenRefreshRequest request) {
    AuthResponse response = authService.refresh(request);
    return ResponseEntity.ok(BaseResponse.success("토큰 재발급에 성공했습니다.", response));
  }

  @Operation(
      summary = "로그아웃",
      description = "전달한 refresh token을 무효화합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/logout")
  public ResponseEntity<BaseResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    authService.logout(userId, request);
    return ResponseEntity.ok(BaseResponse.success("로그아웃에 성공했습니다.", null));
  }
}
