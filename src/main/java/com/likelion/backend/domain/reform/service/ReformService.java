package com.likelion.backend.domain.reform.service;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.service.CatalogService;
import com.likelion.backend.domain.product.entity.DesignOption;
import com.likelion.backend.domain.product.entity.DesignPreviewCache;
import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.repository.DesignOptionRepository;
import com.likelion.backend.domain.product.repository.DesignPreviewCacheRepository;
import com.likelion.backend.domain.product.repository.ProductRepository;
import com.likelion.backend.domain.product.service.DesignPreviewService;
import com.likelion.backend.domain.reform.dto.PriceLineResponse;
import com.likelion.backend.domain.reform.dto.ReformCreateRequest;
import com.likelion.backend.domain.reform.dto.ReformResponse;
import com.likelion.backend.domain.reform.entity.Reform;
import com.likelion.backend.domain.reform.repository.ReformRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReformService {

  private static final String NONE = "NONE";

  private final ReformRepository reformRepository;
  private final ProductRepository productRepository;
  private final DesignOptionRepository designOptionRepository;
  private final DesignPreviewCacheRepository designPreviewCacheRepository;
  private final CatalogService catalogService;
  private final ReformPricingService reformPricingService;

  @Transactional
  public ReformResponse createReform(Long userId, Long productId, ReformCreateRequest request) {
    Product product = getOwnedProduct(userId, productId);
    if (!product.isUpcyclable()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_NOT_UPCYCLABLE);
    }

    DesignOption designOption =
        designOptionRepository
            .findById(request.getDesignOptionId())
            .orElseThrow(() -> new CustomException(GlobalErrorCode.DESIGN_OPTION_NOT_FOUND));
    if (!designOption.getProduct().getId().equals(product.getId())) {
      throw new CustomException(GlobalErrorCode.DESIGN_OPTION_NOT_FOUND);
    }

    ColorSwatchResponse pointColor =
        StringUtils.hasText(request.getPointColor())
            ? catalogService.requirePointColor(request.getPointColor())
            : null;
    ColorSwatchResponse metalColor =
        StringUtils.hasText(request.getMetalColor())
            ? catalogService.requireMetalColor(request.getMetalColor())
            : null;

    AddOnProduct charm = null;
    if (request.getCharmOptionId() != null) {
      charm = catalogService.requireActiveAddOn(request.getCharmOptionId());
      if (charm.getCategory() != AddOnCategory.KEYRING) {
        throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
      }
    }
    AddOnProduct scarf = null;
    if (request.getScarfOptionId() != null) {
      scarf = catalogService.requireActiveAddOn(request.getScarfOptionId());
      if (scarf.getCategory() != AddOnCategory.SCARF) {
        throw new CustomException(GlobalErrorCode.INVALID_INPUT_VALUE);
      }
    }

    final String pointCode = pointColor == null ? null : pointColor.getCode();
    final String metalCode = metalColor == null ? null : metalColor.getCode();

    String previewUrl =
        resolvePreviewImageUrl(
            product.getId(),
            designOption,
            pointCode == null ? NONE : pointCode,
            metalCode == null ? NONE : metalCode,
            charm,
            scarf,
            request.getPreviewImageUrl());

    ReformPricingService.PriceBreakdown pricing =
        reformPricingService.calculate(product.getAiCondition(), charm, scarf);

    final Long charmId = charm == null ? null : charm.getId();
    final String charmName = charm == null ? null : charm.getName();
    final Long scarfId = scarf == null ? null : scarf.getId();
    final String scarfName = scarf == null ? null : scarf.getName();
    final Long designOptionId = designOption.getId();
    final String targetItem = designOption.getName();
    final int repairFee = pricing.repairFee();
    final int totalPrice = pricing.totalPrice();

    // 제품당 Reform 1건: 있으면 덮어쓰기, 없으면 생성 (reformId 유지 -> 예약 연결 안정)
    Reform reform =
        reformRepository
            .findByProduct_Id(product.getId())
            .map(
                existing -> {
                  existing.replaceSelection(
                      designOptionId,
                      targetItem,
                      pointCode,
                      metalCode,
                      charmId,
                      charmName,
                      scarfId,
                      scarfName,
                      previewUrl,
                      repairFee,
                      totalPrice);
                  return existing;
                })
            .orElseGet(
                () ->
                    Reform.builder()
                        .product(product)
                        .designOptionId(designOptionId)
                        .targetItem(targetItem)
                        .pointColor(pointCode)
                        .metalColor(metalCode)
                        .charmOptionId(charmId)
                        .charmOption(charmName)
                        .scarfOptionId(scarfId)
                        .scarfOption(scarfName)
                        .previewImageUrl(previewUrl)
                        .repairFee(repairFee)
                        .totalPrice(totalPrice)
                        .build());

    Reform saved = reformRepository.save(reform);
    return ReformResponse.of(saved, pricing.lines());
  }

  public ReformResponse getReform(Long userId, Long reformId) {
    Reform reform =
        reformRepository
            .findByIdAndProduct_User_Id(reformId, userId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.REFORM_NOT_FOUND));
    return ReformResponse.of(reform, rebuildLines(reform));
  }

  private String resolvePreviewImageUrl(
      Long productId,
      DesignOption designOption,
      String pointColor,
      String metalColor,
      AddOnProduct charm,
      AddOnProduct scarf,
      String clientPreviewUrl) {
    // 1) 클라이언트가 미리보기 결과를 넘긴 경우
    if (StringUtils.hasText(clientPreviewUrl)) {
      return clientPreviewUrl.trim();
    }
    // 2) 동일 조합 캐시
    String charmKey = charm == null ? NONE : String.valueOf(charm.getId());
    String scarfKey = scarf == null ? NONE : String.valueOf(scarf.getId());
    String cacheKey =
        DesignPreviewService.buildCacheKey(
            productId,
            designOption.getId(),
            pointColor,
            metalColor,
            charmKey,
            scarfKey);
    return designPreviewCacheRepository
        .findByCacheKey(cacheKey)
        .map(DesignPreviewCache::getImageUrl)
        .filter(StringUtils::hasText)
        .orElseGet(
            () ->
                StringUtils.hasText(designOption.getImageUrl())
                    ? designOption.getImageUrl()
                    : null);
  }

  private List<PriceLineResponse> rebuildLines(Reform reform) {
    List<PriceLineResponse> lines = new ArrayList<>();
    lines.add(
        PriceLineResponse.builder()
            .name("수선/리폼비")
            .quantity(1)
            .unitPrice(reform.getRepairFee())
            .lineTotal(reform.getRepairFee())
            .build());

    // 가능하면 마스터 가격으로 라인 복원, 없으면 스냅샷 이름 + 잔액
    int remaining = reform.getTotalPrice() - reform.getRepairFee();
    if (reform.getCharmOptionId() != null) {
      try {
        AddOnProduct charm = catalogService.requireActiveAddOn(reform.getCharmOptionId());
        lines.add(
            PriceLineResponse.builder()
                .name(charm.getName())
                .quantity(1)
                .unitPrice(charm.getPrice())
                .lineTotal(charm.getPrice())
                .build());
        remaining -= charm.getPrice();
      } catch (CustomException e) {
        if (reform.getCharmOption() != null) {
          lines.add(
              PriceLineResponse.builder()
                  .name(reform.getCharmOption())
                  .quantity(1)
                  .unitPrice(Math.max(remaining, 0))
                  .lineTotal(Math.max(remaining, 0))
                  .build());
          remaining = 0;
        }
      }
    }
    if (reform.getScarfOptionId() != null) {
      try {
        AddOnProduct scarf = catalogService.requireActiveAddOn(reform.getScarfOptionId());
        lines.add(
            PriceLineResponse.builder()
                .name(scarf.getName())
                .quantity(1)
                .unitPrice(scarf.getPrice())
                .lineTotal(scarf.getPrice())
                .build());
        remaining -= scarf.getPrice();
      } catch (CustomException e) {
        if (reform.getScarfOption() != null && remaining > 0) {
          lines.add(
              PriceLineResponse.builder()
                  .name(reform.getScarfOption())
                  .quantity(1)
                  .unitPrice(remaining)
                  .lineTotal(remaining)
                  .build());
        }
      }
    }
    return lines;
  }

  private Product getOwnedProduct(Long userId, Long productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.PRODUCT_NOT_FOUND));
    if (!product.getUser().getId().equals(userId)) {
      throw new CustomException(GlobalErrorCode.PRODUCT_ACCESS_DENIED);
    }
    return product;
  }
}
