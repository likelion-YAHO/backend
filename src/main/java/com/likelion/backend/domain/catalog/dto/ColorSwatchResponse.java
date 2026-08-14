package com.likelion.backend.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "컬러 스와치")
public class ColorSwatchResponse {

  @Schema(description = "컬러 코드", example = "PINK")
  private String code;

  @Schema(description = "표시 이름", example = "핑크")
  private String label;

  @Schema(description = "HEX", example = "#F48FB1")
  private String hex;
}
