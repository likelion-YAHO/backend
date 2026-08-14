package com.likelion.backend.global.exception;

import com.likelion.backend.global.common.BaseResponse;
import com.likelion.backend.global.exception.model.BaseErrorCode;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
    BaseErrorCode errorCode = ex.getErrorCode();
    log.warn("CustomException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage());
    return ResponseEntity.status(errorCode.getStatus())
        .body(BaseResponse.error(errorCode.getCode(), errorCode.getMessage()));
  }

  // Validation 실패 (@Valid 검증 실패한 요청 body)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<?>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" / "));
    log.warn("Validation 오류 발생: {}", errorMessages);
    return ResponseEntity.badRequest().body(
        BaseResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE.getCode(), errorMessages));
  }

  // 요청 본문 JSON 파싱 실패 (예: 존재하지 않는 enum 값 전달, body 누락)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<BaseResponse<?>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    log.warn("HttpMessageNotReadableException 발생: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(
        BaseResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
            GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()));
  }

  // multipart 요청에 필수 파트(제품/리뷰 사진 등)가 아예 없을 때
  @ExceptionHandler(MissingServletRequestPartException.class)
  protected ResponseEntity<BaseResponse<?>> handleMissingServletRequestPartException(
      MissingServletRequestPartException ex) {
    log.warn("MissingServletRequestPartException 발생: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(
        BaseResponse.error(GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
            "필수 파일이 누락되었습니다: " + ex.getRequestPartName()));
  }

  // 지원하지 않는 HTTP Method 호출 시
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<BaseResponse<?>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("HttpRequestMethodNotSupportedException 발생: {}", ex.getMessage());
    return ResponseEntity.status(GlobalErrorCode.METHOD_NOT_ALLOWED.getStatus())
        .body(BaseResponse.error(GlobalErrorCode.METHOD_NOT_ALLOWED.getCode(),
            GlobalErrorCode.METHOD_NOT_ALLOWED.getMessage()));
  }

  // 메서드 인자 타입이 일치하지 않을 때 (ex. path variable에 문자 전달)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<BaseResponse<?>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    log.warn("MethodArgumentTypeMismatchException 발생: {}", ex.getMessage());
    return ResponseEntity.badRequest().body(
        BaseResponse.error(GlobalErrorCode.TYPE_MISMATCH.getCode(),
            GlobalErrorCode.TYPE_MISMATCH.getMessage()));
  }

  // 정적 파일/경로가 없을 때 (uploads 등)
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<BaseResponse<?>> handleNoResourceFoundException(
      NoResourceFoundException ex) {
    log.warn("NoResourceFoundException 발생: {}", ex.getMessage());
    return ResponseEntity.status(GlobalErrorCode.RESOURCE_NOT_FOUND.getStatus())
        .body(BaseResponse.error(
            GlobalErrorCode.RESOURCE_NOT_FOUND.getCode(),
            GlobalErrorCode.RESOURCE_NOT_FOUND.getMessage()));
  }

  // 예상치 못한 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<?>> handleException(Exception ex) {
    log.error("Server 오류 발생: ", ex);
    return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
        .body(BaseResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
  }
}
