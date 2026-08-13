package com.likelion.backend.domain.reform.controller;

import com.likelion.backend.domain.reform.dto.ReformCreateRequest;
import com.likelion.backend.domain.reform.dto.ReformResponse;
import com.likelion.backend.domain.reform.service.ReformService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reform", description = "리폼 선택 완료 / 견적 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReformController {

  private final ReformService reformService;

  @Operation(
      summary = "리폼 선택 완료 (견적 확정/갱신)",
      description =
          "시안/컬러/추가상품을 확정하고 견적을 저장합니다. "
              + "동일 productId로 다시 호출하면 새 행을 쌓지 않고 기존 Reform을 덮어씁니다(reformId 유지). "
              + "미리보기 URL -> 캐시 -> 시안 이미지 순으로 사용. AI 미호출.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/products/{productId}/reforms")
  public ResponseEntity<BaseResponse<ReformResponse>> createReform(
      @Parameter(description = "제품 ID") @PathVariable Long productId,
      @Valid @RequestBody ReformCreateRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    ReformResponse response = reformService.createReform(userId, productId, request);
    return ResponseEntity.status(HttpStatus.OK)
        .body(BaseResponse.success("리폼 선택이 저장되었습니다.", response));
  }

  @Operation(
      summary = "리폼 상세 조회",
      description = "reformId로 견적·옵션 조회 (예약 생성 시 reformId 사용)",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/reforms/{reformId}")
  public ResponseEntity<BaseResponse<ReformResponse>> getReform(
      @Parameter(description = "리폼 ID") @PathVariable Long reformId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("리폼 상세 조회에 성공했습니다.", reformService.getReform(userId, reformId)));
  }
}
