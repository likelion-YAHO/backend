package com.likelion.backend.domain.lab.controller;

import com.likelion.backend.domain.lab.dto.LabMissionResponseDto;
import com.likelion.backend.domain.lab.service.LabMissionService;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lab", description = "MCM LAB API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lab/missions")
public class LabMissionController {

    private final LabMissionService labMissionService;

    @Operation(summary = "이달의 미션 조회", description = "메인 화면용 월간 소재 및 디자인 주제를 조회합니다.")
    @GetMapping("/current")
    public ResponseEntity<BaseResponse<LabMissionResponseDto>> getCurrentMission() {
        LabMissionResponseDto response = labMissionService.getCurrentMission();

        return ResponseEntity.ok(
                BaseResponse.success("이달의 미션 조회에 성공했습니다.", response)
        );
    }
}