package com.likelion.backend.domain.store.controller;

import com.likelion.backend.domain.store.dto.StoreResponseDto;
import com.likelion.backend.domain.store.service.StoreService;
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

import java.util.List;

@Tag(name = "Store", description = "오프라인 매장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "예약 매장 목록 조회", description = "유저의 위도/경도를 받아 가까운 순서대로 오프라인 매장 리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<StoreResponseDto>>> getStores(
            @Parameter(description = "사용자 위도", example = "37.5665") @RequestParam Double latitude,
            @Parameter(description = "사용자 경도", example = "126.9780") @RequestParam Double longitude) {

        List<StoreResponseDto> response = storeService.getStoresSortedByDistance(latitude, longitude);

        return ResponseEntity.ok(
                BaseResponse.success("예약 매장 목록 조회에 성공했습니다.", response)
        );
    }
}