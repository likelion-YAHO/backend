package com.likelion.backend.domain.reform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "견적 라인 아이템")
public class PriceLineResponse {

  @Schema(description = "항목명", example = "수선비")
  private String name;

  @Schema(description = "수량", example = "1")
  private int quantity;

  @Schema(description = "단가", example = "150000")
  private int unitPrice;

  @Schema(description = "라인 합계", example = "150000")
  private int lineTotal;
}
