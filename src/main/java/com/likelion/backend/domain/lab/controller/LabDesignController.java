package com.likelion.backend.domain.lab.controller;

import com.likelion.backend.domain.lab.dto.*;
import com.likelion.backend.domain.lab.service.LabDesignService;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "AI 디자인 시안 생성 ", description = "프롬프트를 바탕으로 AI 이미지를 생성하여 반환합니다.")
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<AiDesignResponseDto>> generateAiDesign(@Valid @RequestBody AiDesignRequestDto request) {
        AiDesignResponseDto response = labDesignService.generateAiDesign(request);

        return ResponseEntity.ok(
                BaseResponse.success("AI 디자인 시안 생성에 성공했습니다.", response)
        );
    }

    @Operation(summary = "디자인 출품", description = "AI 생성을 거쳐 완성된 디자인을 콘테스트에 제출합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<LabDesignResponseDto>> createLabDesign(
            @Parameter(description = "테스트용 유저 ID", example = "1") @RequestParam(defaultValue = "1") Long userId,
            @RequestBody LabDesignCreateRequestDto request) {

        LabDesignResponseDto response = labDesignService.createLabDesign(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "디자인 출품이 성공적으로 완료되었습니다.", response));
    }

    @Operation(summary = "갤러리 목록 조회", description = "전체 출품작을 최신순(latest) 또는 인기순(popular)으로 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<LabDesignListResponseDto>>> getGalleryList(
            @Parameter(description = "정렬 기준 (latest 또는 popular)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort) {

        List<LabDesignListResponseDto> response = labDesignService.getGalleryList(sort);

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

    @Operation(summary = "디자인 좋아요 토글", description = "갤러리 작품의 좋아요를 켜거나 끕니다.")
    @PostMapping("/{designId}/likes")
    public ResponseEntity<BaseResponse<LabDesignLikeResponseDto>> toggleLike(
            @Parameter(description = "디자인 식별자", example = "1") @PathVariable Long designId,
            @Parameter(description = "테스트용 유저 ID", example = "1") @RequestParam(defaultValue = "1") Long userId) {

        LabDesignLikeResponseDto response = labDesignService.toggleLike(userId, designId);

        return ResponseEntity.ok(
                BaseResponse.success("좋아요 상태가 성공적으로 변경되었습니다.", response)
        );
    }
}