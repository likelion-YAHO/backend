package com.likelion.backend.domain.reform.service;

import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.product.entity.AiCondition;
import com.likelion.backend.domain.reform.dto.PriceLineResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 리폼 견적 계산. 수선비는 상태(상/중/하) 기준 상수 + 추가상품 가격 합산
 */
@Component
public class ReformPricingService {

  public int repairFeeByCondition(AiCondition condition) {
    if (condition == null) {
      return 150_000;
    }
    return switch (condition) {
      case HIGH -> 150_000;
      case MEDIUM -> 180_000;
      case LOW -> 220_000;
    };
  }

  public PriceBreakdown calculate(
      AiCondition condition, AddOnProduct charm, AddOnProduct scarf) {
    int repairFee = repairFeeByCondition(condition);
    List<PriceLineResponse> lines = new ArrayList<>();
    lines.add(
        PriceLineResponse.builder()
            .name("수선·리폼비")
            .quantity(1)
            .unitPrice(repairFee)
            .lineTotal(repairFee)
            .build());

    int total = repairFee;
    if (charm != null) {
      lines.add(
          PriceLineResponse.builder()
              .name(charm.getName())
              .quantity(1)
              .unitPrice(charm.getPrice())
              .lineTotal(charm.getPrice())
              .build());
      total += charm.getPrice();
    }
    if (scarf != null) {
      lines.add(
          PriceLineResponse.builder()
              .name(scarf.getName())
              .quantity(1)
              .unitPrice(scarf.getPrice())
              .lineTotal(scarf.getPrice())
              .build());
      total += scarf.getPrice();
    }
    return new PriceBreakdown(repairFee, total, lines);
  }

  public record PriceBreakdown(int repairFee, int totalPrice, List<PriceLineResponse> lines) {}
}
