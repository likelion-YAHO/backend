package com.likelion.backend.domain.user.controller;

import com.likelion.backend.domain.user.dto.UpdateAlarmRequest;
import com.likelion.backend.domain.user.dto.UpdateProfileRequest;
import com.likelion.backend.domain.user.dto.UserMeResponse;
import com.likelion.backend.domain.user.service.UserService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

  @Operation(
      summary = "내 프로필 수정",
      description = "이름, 이메일, 전화번호를 부분 수정합니다. 요청에 포함된 필드만 변경됩니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PatchMapping("/me")
  public ResponseEntity<BaseResponse<UserMeResponse>> updateMe(
      @Valid @RequestBody UpdateProfileRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("프로필 수정에 성공했습니다.", userService.updateMe(userId, request)));
  }

  @Operation(
      summary = "프로필 이미지 변경",
      description = "프로필 이미지를 업로드합니다. jpeg/png/webp, 최대 10MB.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<UserMeResponse>> updateProfileImage(
      @Parameter(description = "프로필 이미지", required = true)
      @RequestPart("image") MultipartFile image) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success(
            "프로필 이미지 변경에 성공했습니다.", userService.updateProfileImage(userId, image)));
  }

  @Operation(
      summary = "알림 수신 여부 변경",
      description = "alarmEnabled=true면 수신, false면 알림 끄기. 화면의 '알림 끄기' 토글 ON은 false를 보냅니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PatchMapping("/me/alarm")
  public ResponseEntity<BaseResponse<UserMeResponse>> updateAlarm(
      @Valid @RequestBody UpdateAlarmRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("알림 설정 변경에 성공했습니다.", userService.updateAlarm(userId, request)));
  }
}
