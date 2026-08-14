package com.likelion.backend.domain.product.dto;

import com.likelion.backend.domain.product.entity.DesignOption;
import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "제품 응답")
public class ProductResponse {

  @Schema(description = "제품 ID", example = "12")
  private Long productId;

  @Schema(description = "카테고리 코드", example = "BACKPACK")
  private String category;

  @Schema(description = "카테고리 한글명", example = "백팩")
  private String categoryLabel;

  @Schema(description = "AI 상태 코드", example = "HIGH")
  private String aiCondition;

  @Schema(description = "AI 상태 한글", example = "상")
  private String aiConditionLabel;

  @Schema(description = "업사이클 가능 여부", example = "true")
  private boolean upcyclable;

  @Schema(description = "재활용 가능 부위 설명")
  private String recyclableParts;

  @Schema(description = "상태+제품 종류 기준 권장 시안 규모 힌트")
  private String sizeHint;

  @Schema(description = "상태 분석 메시지", example = "완전한 모습의 제품으로 판단되었습니다.")
  private String message;

  @Schema(description = "사용자 디자인 프롬프트 (아직 미입력 시 null)")
  private String userPrompt;

  @Schema(description = "product 단위 AI 추천 키링/참 ID (design-analysis 후 설정)", example = "3")
  private Long recommendedCharmId;

  @Schema(description = "product 단위 AI 추천 키링/참 이름", example = "베어 키링")
  private String recommendedCharmName;

  @Schema(description = "product 단위 AI 추천 스카프 ID", example = "5")
  private Long recommendedScarfId;

  @Schema(description = "product 단위 AI 추천 스카프 이름", example = "레드 모노그램 스카프")
  private String recommendedScarfName;

  @Schema(description = "제품 이미지 목록")
  private List<ProductImageResponse> images;

  @Schema(description = "추천 시안 목록 (없으면 빈 배열)")
  private List<DesignOptionResponse> designOptions;

  @Schema(description = "등록 시각")
  private LocalDateTime createdAt;

  public static ProductResponse of(
      Product product,
      List<ProductImage> images,
      List<DesignOption> designOptions,
      String analysisMessage) {
    List<DesignOption> options =
        designOptions == null ? Collections.emptyList() : designOptions;
    return ProductResponse.builder()
        .productId(product.getId())
        .category(product.getCategory().name())
        .categoryLabel(product.getCategory().getLabel())
        .aiCondition(product.getAiCondition().name())
        .aiConditionLabel(product.getAiCondition().getLabel())
        .upcyclable(product.isUpcyclable())
        .recyclableParts(product.getRecyclableParts())
        .sizeHint(product.getSizeHint())
        .message(analysisMessage)
        .userPrompt(product.getUserPrompt())
        .recommendedCharmId(product.getRecommendedCharmId())
        .recommendedCharmName(product.getRecommendedCharmName())
        .recommendedScarfId(product.getRecommendedScarfId())
        .recommendedScarfName(product.getRecommendedScarfName())
        .images(images.stream().map(ProductImageResponse::from).toList())
        .designOptions(options.stream().map(DesignOptionResponse::from).toList())
        .createdAt(product.getCreatedAt())
        .build();
  }

  public static ProductResponse of(Product product, List<ProductImage> images) {
    return of(product, images, Collections.emptyList(), null);
  }

  public static ProductResponse of(
      Product product, List<ProductImage> images, String analysisMessage) {
    return of(product, images, Collections.emptyList(), analysisMessage);
  }

  public static ProductResponse of(
      Product product, List<ProductImage> images, List<DesignOption> designOptions) {
    return of(product, images, designOptions, null);
  }
}
