package com.likelion.backend.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "추가상품 목록 응답")
public class AddOnProductListResponse {

  @Schema(description = "추가상품 목록")
  private List<AddOnProductResponse> items;
}
