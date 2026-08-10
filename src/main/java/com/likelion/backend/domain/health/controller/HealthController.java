package com.likelion.backend.domain.health.controller;

import com.likelion.backend.domain.health.dto.HealthResDto;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서버 상태 확인용 헬스체크 API
 */
@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
public class HealthController {

    @Operation(summary = "헬스체크", description = "서버가 정상적으로 응답하는지 확인한다. 배포/인프라 확인용으로 사용한다.")
    @GetMapping("/health")
    public BaseResponse<HealthResDto> health() {
        return BaseResponse.success(HealthResDto.ok());
    }
}
