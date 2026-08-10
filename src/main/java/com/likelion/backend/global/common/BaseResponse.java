package com.likelion.backend.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "BaseResponse DTO", description = "공통 API 응답 형식")
public class BaseResponse<T> {

  @Schema(description = "요청 성공 여부", example = "true")
  private boolean success;

  @Schema(description = "성공 시 HTTP 상태 코드(200), 실패 시 에러 코드(ex. P001)", example = "200")
  private Object code;

  @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
  private String message;

  @Schema(description = "응답 데이터")
  private T data;

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(true, 200, "요청이 성공적으로 처리되었습니다.", data);
  }

  public static <T> BaseResponse<T> success(String message, T data) {
    return new BaseResponse<>(true, 200, message, data);
  }

  public static <T> BaseResponse<T> success(int code, String message, T data) {
    return new BaseResponse<>(true, code, message, data);
  }

  public static <T> BaseResponse<T> error(String code, String message) {
    return new BaseResponse<>(false, code, message, null);
  }
}
