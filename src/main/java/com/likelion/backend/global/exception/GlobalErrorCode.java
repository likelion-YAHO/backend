package com.likelion.backend.global.exception;

import com.likelion.backend.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
  INVALID_INPUT_VALUE("G001", "유효하지 않은 입력입니다.", HttpStatus.BAD_REQUEST),
  RESOURCE_NOT_FOUND("G002", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INTERNAL_SERVER_ERROR("G003", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  METHOD_NOT_ALLOWED("G004", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
  TYPE_MISMATCH("G005", "요청 인자의 데이터 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

  // 인증 / 회원 도메인 에러코드
  UNAUTHORIZED("A001", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
  INVALID_CREDENTIALS("A002", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_REFRESH_TOKEN("A003", "유효하지 않거나 만료된 refresh token입니다.", HttpStatus.UNAUTHORIZED),
  EMAIL_ALREADY_EXISTS("A004", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
  USER_NOT_FOUND("A005", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),

  // 제품 등록 도메인 에러코드
  PRODUCT_NOT_FOUND("P001", "존재하지 않는 제품입니다.", HttpStatus.NOT_FOUND),
  PRODUCT_NOT_UPCYCLABLE("P002", "업사이클링이 불가능한 제품입니다.", HttpStatus.BAD_REQUEST),
  DESIGN_OPTION_NOT_FOUND("P003", "존재하지 않는 디자인 옵션입니다.", HttpStatus.NOT_FOUND),
  PRODUCT_IMAGE_LIMIT_EXCEEDED("P004", "제품 이미지는 최대 5장까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST),
  PRODUCT_IMAGE_REQUIRED("P005", "제품 이미지는 최소 1장 이상 필요합니다.", HttpStatus.BAD_REQUEST),
  INVALID_IMAGE_TYPE("P006", "지원하지 않는 이미지 형식입니다. (jpeg, png, webp만 허용)", HttpStatus.BAD_REQUEST),
  PRODUCT_ACCESS_DENIED("P007", "해당 제품에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),
  FILE_UPLOAD_FAILED("P008", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_PRODUCT_CATEGORY("P009", "존재하지 않는 제품 카테고리입니다.", HttpStatus.BAD_REQUEST),
  AI_ANALYSIS_FAILED("P010", "AI 상태 분석에 실패했습니다.", HttpStatus.BAD_GATEWAY),
  AI_NOT_CONFIGURED("P011", "AI API 키가 설정되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DESIGN_PROMPT_REQUIRED("P012", "디자인 가이드 프롬프트는 필수입니다.", HttpStatus.BAD_REQUEST),
  AI_DESIGN_FAILED("P013", "AI 시안 추천에 실패했습니다.", HttpStatus.BAD_GATEWAY),
  INVALID_COLOR("P014", "유효하지 않은 컬러 코드입니다.", HttpStatus.BAD_REQUEST),
  AI_PREVIEW_FAILED("P015", "미리보기 이미지 생성에 실패했습니다.", HttpStatus.BAD_GATEWAY),

  // 매장 도메인 에러코드
  STORE_NOT_FOUND("S001", "존재하지 않는 매장입니다.", HttpStatus.NOT_FOUND),

  // 리폼 / 예약 도메인 에러코드
  RESERVATION_NOT_FOUND("R001", "존재하지 않는 예약입니다.", HttpStatus.NOT_FOUND),
  ADD_ON_PRODUCT_NOT_FOUND("R002", "존재하지 않는 추가상품입니다.", HttpStatus.NOT_FOUND),
  RESERVATION_ALREADY_CANCELLED("R003", "이미 취소된 예약입니다.", HttpStatus.CONFLICT),
  REFORM_NOT_FOUND("R004", "존재하지 않는 리폼(선택 완료) 내역입니다.", HttpStatus.NOT_FOUND),
  REFORM_ACCESS_DENIED("R005", "해당 리폼에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),

  // 리뷰 도메인 에러코드
  REVIEW_NOT_FOUND("V001", "존재하지 않는 리뷰입니다.", HttpStatus.NOT_FOUND),
  REVIEW_ALREADY_EXISTS("V002", "이미 리뷰가 작성된 예약입니다.", HttpStatus.CONFLICT),

  // 문의 도메인 에러코드
  INQUIRY_NOT_FOUND("I001", "존재하지 않는 문의입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
