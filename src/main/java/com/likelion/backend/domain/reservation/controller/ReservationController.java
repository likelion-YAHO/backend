package com.likelion.backend.domain.reservation.controller;

import com.likelion.backend.domain.reservation.dto.ReservationCreateRequest;
import com.likelion.backend.domain.reservation.dto.ReservationDetailResponseDto;
import com.likelion.backend.domain.reservation.dto.ReservationResponseDto;
import com.likelion.backend.domain.reservation.service.ReservationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservation", description = "예약 API")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "내 예약 목록 조회",
            description = "현재 로그인 사용자의 예약 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<ReservationResponseDto>>> getMyReservations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                BaseResponse.success("예약 목록 조회에 성공했습니다.", reservationService.getMyReservations(userId))
        );
    }

    @Operation(
            summary = "[사용자용] 예약 상세 조회",
            description = "본인 예약의 상세 정보(타임라인, 바코드 주문번호 등)를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDetailResponseDto>> getReservationDetail(
            @Parameter(description = "예약 식별자", example = "1") @PathVariable Long reservationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                BaseResponse.success(
                        "예약 상세 조회에 성공했습니다.",
                        reservationService.getReservationDetail(reservationId, userId))
        );
    }

    @Operation(summary = "[매장 직원용] 바코드 스캔 조회 및 수령 처리", description = "주문 번호(예: UPC-7K4D-92LM)를 통해 예약 정보를 조회하고 수령 완료 상태로 변경합니다.")
    @GetMapping("/scan/{orderNumber}")
    public ResponseEntity<BaseResponse<ReservationDetailResponseDto>> getReservationByOrderNumber(
            @Parameter(description = "주문 번호", example = "UPC-7K4D-92LM") @PathVariable String orderNumber) {
        return ResponseEntity.ok(
                BaseResponse.success("바코드 스캔 및 수령 처리에 성공했습니다.", reservationService.scanBarcodeAndUpdateStatus(orderNumber))
        );
    }

    @Operation(
            summary = "예약 접수",
            description = "제품, 매장, 방문 날짜를 선택하여 새로운 수선/업사이클링 예약을 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BaseResponse<ReservationDetailResponseDto>> createReservation(
            @Valid @RequestBody ReservationCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReservationDetailResponseDto response = reservationService.createReservation(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "예약이 성공적으로 접수되었습니다.", response));
    }

    @Operation(
            summary = "예약 취소",
            description = "예약 ID를 통해 본인 예약을 취소 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<BaseResponse<Void>> cancelReservation(
            @Parameter(description = "예약 식별자", example = "1") @PathVariable Long reservationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        reservationService.cancelReservation(reservationId, userId);

        return ResponseEntity.ok(
                BaseResponse.success("예약이 성공적으로 취소되었습니다.", null)
        );
    }

    @Operation(
            summary = "예약 변경",
            description = "예약 ID를 통해 본인 예약의 방문 날짜나 매장 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDetailResponseDto>> updateReservation(
            @Parameter(description = "예약 식별자", example = "1") @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReservationDetailResponseDto response = reservationService.updateReservation(reservationId, request, userId);

        return ResponseEntity.ok(
                BaseResponse.success("예약이 성공적으로 변경되었습니다.", response)
        );
    }

    @Operation(
            summary = "예약 복원",
            description = "취소된 본인 예약을 다시 접수 상태로 복원합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{reservationId}/restore")
    public ResponseEntity<BaseResponse<Void>> restoreReservation(
            @Parameter(description = "예약 식별자", example = "1") @PathVariable Long reservationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        reservationService.restoreReservation(reservationId, userId);

        return ResponseEntity.ok(
                BaseResponse.success("예약이 성공적으로 복원되었습니다.", null)
        );
    }
}
