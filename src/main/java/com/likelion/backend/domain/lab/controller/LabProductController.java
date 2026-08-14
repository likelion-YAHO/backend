package com.likelion.backend.domain.lab.controller;

import com.likelion.backend.domain.lab.dto.BaseProductResponseDto;
import com.likelion.backend.domain.lab.service.LabProductService;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Lab", description = "MCM LAB API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab/base-products")
public class LabProductController {

    private final LabProductService labProductService;

    @Operation(summary = "베이스 제품 목록 조회", description = "가상 커스텀을 시작할 원본 가방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<BaseProductResponseDto>>> getBaseProducts() {
        return ResponseEntity.ok(
                BaseResponse.success("베이스 제품 목록 조회에 성공했습니다.", labProductService.getBaseProducts())
        );
    }
}