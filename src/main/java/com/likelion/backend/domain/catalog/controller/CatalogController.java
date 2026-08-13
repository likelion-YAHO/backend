package com.likelion.backend.domain.catalog.controller;

import com.likelion.backend.domain.catalog.dto.AddOnProductListResponse;
import com.likelion.backend.domain.catalog.dto.ColorListResponse;
import com.likelion.backend.domain.catalog.service.CatalogService;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Catalog", description = "컬러 스와치 / 추가상품 마스터 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService catalogService;

  @Operation(summary = "포인트 컬러 목록", description = "제품 포인트 스와치 선택지")
  @GetMapping("/colors/point")
  public ResponseEntity<BaseResponse<ColorListResponse>> getPointColors() {
    return ResponseEntity.ok(
        BaseResponse.success("포인트 컬러 조회에 성공했습니다.", catalogService.getPointColors()));
  }

  @Operation(summary = "메탈 컬러 목록", description = "골드/실버 메탈 선택지")
  @GetMapping("/colors/metal")
  public ResponseEntity<BaseResponse<ColorListResponse>> getMetalColors() {
    return ResponseEntity.ok(
        BaseResponse.success("메탈 컬러 조회에 성공했습니다.", catalogService.getMetalColors()));
  }

  @Operation(
      summary = "추가상품 목록",
      description = "레더 참/키링, 스카프 등. category=KEYRING|SCARF 로 필터 가능")
  @GetMapping("/add-on-products")
  public ResponseEntity<BaseResponse<AddOnProductListResponse>> getAddOnProducts(
      @Parameter(description = "KEYRING 또는 SCARF (미입력 시 전체)")
          @RequestParam(required = false)
          String category) {
    return ResponseEntity.ok(
        BaseResponse.success(
            "추가상품 목록 조회에 성공했습니다.", catalogService.getAddOnProducts(category)));
  }
}
