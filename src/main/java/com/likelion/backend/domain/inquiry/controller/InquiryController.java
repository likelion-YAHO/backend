package com.likelion.backend.domain.inquiry.controller;

import com.likelion.backend.domain.inquiry.dto.InquiryRequestDto;
import com.likelion.backend.domain.inquiry.service.InquiryService;
import com.likelion.backend.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry", description = "문의 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "상담원에게 문의 등록", description = "주문 번호와 유저 ID를 바탕으로 상담원에게 문의 내용을 전송합니다.")
    @PostMapping("/{orderNumber}/inquiries")
    public ResponseEntity<BaseResponse<Void>> createInquiry(
            @Parameter(description = "테스트용 유저 ID", example = "1") @RequestParam(defaultValue = "1") Long userId,
            @PathVariable String orderNumber,
            @Valid @RequestBody InquiryRequestDto request) {

        inquiryService.createInquiry(userId, orderNumber, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "문의 내용이 성공적으로 전송되었습니다.", null));
    }
}