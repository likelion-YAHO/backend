package com.likelion.backend.domain.lab.controller;

import com.likelion.backend.domain.lab.dto.LabEditionResponseDto;
import com.likelion.backend.domain.lab.service.LabDesignService;
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
@RequestMapping("/api/lab/editions")
public class LabEditionController {

    private final LabDesignService labDesignService;

    @Operation(summary = "랩 에디션 조회", description = "실물 제작이 확정되거나 판매 중인 한정판(Edition) 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<LabEditionResponseDto>>> getLabEditions() {

        List<LabEditionResponseDto> response = labDesignService.getLabEditions();

        return ResponseEntity.ok(
                BaseResponse.success("랩 에디션 목록 조회에 성공했습니다.", response)
        );
    }
}