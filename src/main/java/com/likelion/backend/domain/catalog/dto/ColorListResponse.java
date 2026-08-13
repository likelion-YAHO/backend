package com.likelion.backend.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "컬러 목록 응답")
public class ColorListResponse {

  @Schema(description = "컬러 스와치 목록")
  private List<ColorSwatchResponse> colors;
}
