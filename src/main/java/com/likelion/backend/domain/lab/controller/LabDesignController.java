package com.likelion.backend.domain.lab.controller;

import com.likelion.backend.domain.lab.dto.*;
import com.likelion.backend.domain.lab.service.LabDesignPreviewService;
import com.likelion.backend.domain.lab.service.LabDesignService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Tag(name = "Lab", description = "MCM LAB API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab/designs")
public class LabDesignController {

    private final LabDesignService labDesignService;
    private final LabDesignPreviewService labDesignPreviewService;

    @Operation(
            summary = "AI 디자인 시안 생성",
            description = "프롬프트를 바탕으로 AI 이미지를 생성합니다. 로그인 사용자 + 이달의 미션 + 베이스 제품당 최대 3회입니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<AiDesignResponseDto>> generateAiDesign(
            @Valid @RequestBody AiDesignRequestDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiDesignResponseDto response = labDesignService.generateAiDesign(userId, request);

        return ResponseEntity.ok(
                BaseResponse.success("AI 디자인 시안 생성에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "커스텀 미리보기",
            description =
                    "generate 시안 + 포인트/메탈/추가상품 조합으로 미리보기 이미지를 생성합니다. "
                            + "같은 조합은 DB 캐시를 재사용하며 cacheHit=true로 표시됩니다. ",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/preview")
    public ResponseEntity<BaseResponse<LabDesignPreviewResponse>> createDesignPreview(
            @Valid @RequestBody LabDesignPreviewRequest request) {
        SecurityUtils.getCurrentUserId();
        LabDesignPreviewResponse response = labDesignPreviewService.createPreview(request);
        String message =
                response.isCacheHit()
                        ? "캐시된 미리보기를 반환했습니다."
                        : "미리보기 이미지 생성에 성공했습니다.";
        return ResponseEntity.ok(BaseResponse.success(message, response));
    }

    @Operation(
            summary = "디자인 출품",
            description = "AI 생성을 거쳐 완성된 디자인을 콘테스트에 제출합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BaseResponse<LabDesignResponseDto>> createLabDesign(
            @RequestBody LabDesignCreateRequestDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LabDesignResponseDto response = labDesignService.createLabDesign(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "디자인 출품이 성공적으로 완료되었습니다.", response));
    }

    @Operation(summary = "갤러리 목록 조회", description = "전체 출품작을 최신순(latest) 또는 인기순(popular)으로 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<LabDesignListResponseDto>>> getGalleryList(
            @Parameter(description = "정렬 기준 (latest 또는 popular)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort) {

        Long userId = SecurityUtils.getCurrentUserId();

        List<LabDesignListResponseDto> response = labDesignService.getGalleryList(userId, sort);

        return ResponseEntity.ok(
                BaseResponse.success("갤러리 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(summary = "디자인 상세 조회", description = "갤러리 특정 작품의 상세 정보, 제작 진행 상태, 판매 정보를 조회합니다.")
    @GetMapping("/{designId}")
    public ResponseEntity<BaseResponse<LabDesignDetailResponseDto>> getDesignDetail(
            @Parameter(description = "디자인 식별자", example = "1") @PathVariable Long designId) {

        LabDesignDetailResponseDto response = labDesignService.getDesignDetail(designId);

        return ResponseEntity.ok(
                BaseResponse.success("디자인 상세 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "출품작 삭제",
            description = "본인이 제출한 가상 출품작을 삭제합니다. 선정/판매 에디션은 삭제할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{designId}")
    public ResponseEntity<BaseResponse<Void>> deleteLabDesign(
            @Parameter(description = "디자인 식별자", example = "1") @PathVariable Long designId) {
        Long userId = SecurityUtils.getCurrentUserId();
        labDesignService.deleteLabDesign(userId, designId);
        return ResponseEntity.ok(BaseResponse.success("출품작이 삭제되었습니다.", null));
    }

    @Operation(
            summary = "디자인 좋아요 토글",
            description = "갤러리 작품의 좋아요를 켜거나 끕니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{designId}/likes")
    public ResponseEntity<BaseResponse<LabDesignLikeResponseDto>> toggleLike(
            @Parameter(description = "디자인 식별자", example = "1") @PathVariable Long designId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LabDesignLikeResponseDto response = labDesignService.toggleLike(userId, designId);

        return ResponseEntity.ok(
                BaseResponse.success("좋아요 상태가 성공적으로 변경되었습니다.", response)
        );
    }
}
