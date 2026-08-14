package com.likelion.backend.domain.inquiry.controller;

import com.likelion.backend.domain.inquiry.dto.InquiryRequestDto;
import com.likelion.backend.domain.inquiry.service.InquiryService;
import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(
            summary = "상담원에게 문의 등록",
            description = "현재 로그인 사용자가 주문 번호로 문의 내용을 전송합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{orderNumber}/inquiries")
    public ResponseEntity<BaseResponse<Void>> createInquiry(
            @PathVariable String orderNumber,
            @Valid @RequestBody InquiryRequestDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        inquiryService.createInquiry(userId, orderNumber, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "문의 내용이 성공적으로 전송되었습니다.", null));
    }
}
