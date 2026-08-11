package com.likelion.backend.domain.user.controller;

import com.likelion.backend.domain.user.dto.UserMeResponse;
import com.likelion.backend.domain.user.service.UserService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 프로필 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "내 프로필 조회",
      description = "access token 기준 현재 로그인 사용자 정보를 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/me")
  public ResponseEntity<BaseResponse<UserMeResponse>> getMe() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("내 프로필 조회에 성공했습니다.", userService.getMe(userId)));
  }
}
