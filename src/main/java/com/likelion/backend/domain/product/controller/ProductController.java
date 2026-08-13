package com.likelion.backend.domain.product.controller;

import com.likelion.backend.domain.product.dto.DesignAnalysisRequest;
import com.likelion.backend.domain.product.dto.DesignAnalysisResponse;
import com.likelion.backend.domain.product.dto.DesignPreviewRequest;
import com.likelion.backend.domain.product.dto.DesignPreviewResponse;
import com.likelion.backend.domain.product.dto.ProductResponse;
import com.likelion.backend.domain.product.service.DesignPreviewService;
import com.likelion.backend.domain.product.service.ProductService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product", description = "제품 등록 / AI 상태/시안 분석 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final DesignPreviewService designPreviewService;

  @Operation(
      summary = "제품 등록 + AI 상태 분석",
      description = "제품 사진(1~5장)과 카테고리를 등록하고, AI가 훼손 상태(상/중/하)와 업사이클 가능 여부를 판정합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<ProductResponse>> createProduct(
      @Parameter(description = "제품 카테고리 (예: BACKPACK/TOTE_SHOULDER/SHOULDER_CROSS/MINI_BAG/CLUTCH_POUCH/CLOTHING/STRAP_ACCESSORY)", required = true)
      @RequestPart("category") String category,
      @Parameter(description = "제품 이미지 1~5장", required = true)
      @RequestPart("images") List<MultipartFile> images) {

    Long userId = SecurityUtils.getCurrentUserId();
    ProductResponse response = productService.createProduct(userId, category, images);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(201, "제품 등록 및 AI 상태 분석에 성공했습니다.", response));
  }

  @Operation(
      summary = "내 제품 목록 조회",
      description = "로그인한 사용자가 등록한 제품 목록을 최신순으로 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping
  public ResponseEntity<BaseResponse<List<ProductResponse>>> getMyProducts() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("내 제품 목록 조회에 성공했습니다.", productService.getMyProducts(userId)));
  }

  @Operation(
      summary = "제품 상세 조회",
      description = "제품 ID로 상세 정보(이미지, AI 상태, 시안 목록 등)를 조회합니다. (본인 제품만 조회)",
      security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping("/{productId}")
  public ResponseEntity<BaseResponse<ProductResponse>> getProduct(
      @Parameter(description = "제품 ID", example = "1") @PathVariable Long productId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        BaseResponse.success("제품 상세 조회에 성공했습니다.", productService.getProduct(userId, productId)));
  }

  @Operation(
      summary = "디자인 시안 추천",
      description =
          "사용자 디자인 가이드 프롬프트를 저장하고, 제품 사진/카테고리/상태를 바탕으로 AI 리폼 시안을 추천합니다. 재호출 시 기존 시안은 교체됩니다.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/{productId}/design-analysis")
  public ResponseEntity<BaseResponse<DesignAnalysisResponse>> analyzeDesign(
      @Parameter(description = "제품 ID", example = "1") @PathVariable Long productId,
      @Valid @RequestBody DesignAnalysisRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    DesignAnalysisResponse response =
        productService.analyzeDesign(userId, productId, request.getUserPrompt());
    return ResponseEntity.ok(BaseResponse.success("AI 시안 추천에 성공했습니다.", response));
  }

  @Operation(
      summary = "커스텀 미리보기",
      description =
          "시안/포인트/메탈/추가상품 조합으로 미리보기 이미지를 생성합니다. "
              + "같은 조합은 DB 캐시를 재사용하며 cacheHit=true로 표시됩니다. "
              + "스와치 변경마다 호출 X, 미리보기 버튼에서만 호출 O.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/{productId}/design-preview")
  public ResponseEntity<BaseResponse<DesignPreviewResponse>> createDesignPreview(
      @Parameter(description = "제품 ID", example = "1") @PathVariable Long productId,
      @Valid @RequestBody DesignPreviewRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    DesignPreviewResponse response =
        designPreviewService.createPreview(userId, productId, request);
    String message =
        response.isCacheHit()
            ? "캐시된 미리보기를 반환했습니다."
            : "미리보기 이미지 생성에 성공했습니다.";
    return ResponseEntity.ok(BaseResponse.success(message, response));
  }

  @Operation(
      summary = "제품 삭제",
      description = "등록한 제품, 시안, 로컬 이미지를 삭제합니다. (본인 제품만 삭제 가능)",
      security = @SecurityRequirement(name = "bearerAuth"))
  @DeleteMapping("/{productId}")
  public ResponseEntity<BaseResponse<Void>> deleteProduct(
      @Parameter(description = "제품 ID", example = "1") @PathVariable Long productId) {
    Long userId = SecurityUtils.getCurrentUserId();
    productService.deleteProduct(userId, productId);
    return ResponseEntity.ok(BaseResponse.success("제품 삭제에 성공했습니다.", null));
  }
}
