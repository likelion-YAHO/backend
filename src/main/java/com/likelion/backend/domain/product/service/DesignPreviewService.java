package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.service.CatalogService;
import com.likelion.backend.domain.preview.AddonCompositePreviewService;
import com.likelion.backend.domain.preview.PreviewComposeCommand;
import com.likelion.backend.domain.product.dto.DesignPreviewRequest;
import com.likelion.backend.domain.product.dto.DesignPreviewResponse;
import com.likelion.backend.domain.product.entity.DesignOption;
import com.likelion.backend.domain.product.entity.DesignPreviewCache;
import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import com.likelion.backend.domain.product.repository.DesignOptionRepository;
import com.likelion.backend.domain.product.repository.DesignPreviewCacheRepository;
import com.likelion.backend.domain.product.repository.ProductImageRepository;
import com.likelion.backend.domain.product.repository.ProductRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignPreviewService {

  private static final String PREVIEW_DIR = "previews";
  private static final String NONE = "NONE";

  private final ProductRepository productRepository;
  private final DesignOptionRepository designOptionRepository;
  private final ProductImageRepository productImageRepository;
  private final DesignPreviewCacheRepository designPreviewCacheRepository;
  private final CatalogService catalogService;
  private final AddonCompositePreviewService addonCompositePreviewService;

  @Transactional
  public DesignPreviewResponse createPreview(
      Long userId, Long productId, DesignPreviewRequest request) {
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

    String pointKey = pointColor == null ? NONE : pointColor.getCode();
    String metalKey = metalColor == null ? NONE : metalColor.getCode();
    String charmKey = charm == null ? NONE : String.valueOf(charm.getId());
    String scarfKey = scarf == null ? NONE : String.valueOf(scarf.getId());
    String cacheKey =
        buildCacheKey(
            product.getId(),
            designOption.getId(),
            pointKey,
            metalKey,
            charmKey,
            scarfKey);

    var cached = designPreviewCacheRepository.findByCacheKey(cacheKey);
    if (cached.isPresent()) {
      return toResponse(
          product.getId(),
          true,
          cached.get().getImageUrl(),
          cacheKey,
          designOption.getId(),
          pointColor == null ? null : pointColor.getCode(),
          metalColor == null ? null : metalColor.getCode(),
          charm,
          scarf);
    }

    String imageUrl =
        addonCompositePreviewService.compose(
            PreviewComposeCommand.builder()
                .sourceImageUrl(resolveSourceImageUrl(product, designOption))
                .uploadDirectory(PREVIEW_DIR)
                .baseDesignName(designOption.getName())
                .baseDesignDescription(designOption.getDescription())
                .pointColor(pointColor)
                .metalColor(metalColor)
                .charm(charm)
                .scarf(scarf)
                .build());

    DesignPreviewCache saved =
        designPreviewCacheRepository.save(
            DesignPreviewCache.builder()
                .productId(product.getId())
                .designOptionId(designOption.getId())
                .pointColor(pointKey)
                .metalColor(metalKey)
                .charmOptionId(charmKey)
                .scarfOptionId(scarfKey)
                .cacheKey(cacheKey)
                .imageUrl(imageUrl)
                .build());

    return toResponse(
        product.getId(),
        false,
        saved.getImageUrl(),
        cacheKey,
        designOption.getId(),
        pointColor == null ? null : pointColor.getCode(),
        metalColor == null ? null : metalColor.getCode(),
        charm,
        scarf);
  }

  private String resolveSourceImageUrl(Product product, DesignOption designOption) {
    if (StringUtils.hasText(designOption.getImageUrl())) {
      return designOption.getImageUrl();
    }
    List<ProductImage> images =
        productImageRepository.findAllByProductIdOrderBySortOrderAsc(product.getId());
    if (images.isEmpty()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_REQUIRED);
    }
    return images.get(0).getImageUrl();
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

  public static String buildCacheKey(
      Long productId,
      Long designOptionId,
      String pointColor,
      String metalColor,
      String charmKey,
      String scarfKey) {
    return String.join(
        ":",
        String.valueOf(productId),
        String.valueOf(designOptionId),
        pointColor.toUpperCase(Locale.ROOT),
        metalColor.toUpperCase(Locale.ROOT),
        charmKey,
        scarfKey);
  }

  private DesignPreviewResponse toResponse(
      Long productId,
      boolean cacheHit,
      String imageUrl,
      String cacheKey,
      Long designOptionId,
      String pointColor,
      String metalColor,
      AddOnProduct charm,
      AddOnProduct scarf) {
    return DesignPreviewResponse.builder()
        .productId(productId)
        .cacheHit(cacheHit)
        .previewImageUrl(imageUrl)
        .cacheKey(cacheKey)
        .designOptionId(designOptionId)
        .pointColor(pointColor)
        .metalColor(metalColor)
        .charmOptionId(charm == null ? null : charm.getId())
        .scarfOptionId(scarf == null ? null : scarf.getId())
        .charmOptionName(charm == null ? null : charm.getName())
        .scarfOptionName(scarf == null ? null : scarf.getName())
        .build();
  }
}
