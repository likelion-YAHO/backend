package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.service.CatalogService;
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
import com.likelion.backend.global.config.AiProperties;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import com.likelion.backend.global.storage.FileStorageService;
import com.likelion.backend.global.storage.StaticResourceLoader;
import com.likelion.backend.global.storage.StoredFile;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
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
  private final FileStorageService fileStorageService;
  private final StaticResourceLoader staticResourceLoader;
  private final AiProperties aiProperties;
  private final JsonMapper jsonMapper;

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
        generatePreviewImage(product, designOption, pointColor, metalColor, charm, scarf);

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

  private String generatePreviewImage(
      Product product,
      DesignOption designOption,
      ColorSwatchResponse pointColor,
      ColorSwatchResponse metalColor,
      AddOnProduct charm,
      AddOnProduct scarf) {
    String baseDesignUrl = resolveSourceImageUrl(product, designOption);

    if (!aiProperties.isEnabled()
        || !aiProperties.isDesignImageEnabled()
        || !StringUtils.hasText(aiProperties.getApiKey())) {
      log.info("미리보기 AI 생략(캐시 miss, 시안 이미지 사용). productId={}", product.getId());
      return baseDesignUrl;
    }

    try {
      StoredFile baseDesign = loadImage(baseDesignUrl);
      List<Map<String, Object>> imageRefs = new ArrayList<>();
      imageRefs.add(toImageRef(baseDesign));

      // 참조 이미지 순서: 1=시안(유지), 2=키링(원본 그대로 배치), 3=스카프(원본 그대로 배치)
      Optional<StoredFile> charmFile = loadAddOnImage(charm);
      Optional<StoredFile> scarfFile = loadAddOnImage(scarf);
      charmFile.ifPresent(f -> imageRefs.add(toImageRef(f)));
      scarfFile.ifPresent(f -> imageRefs.add(toImageRef(f)));

      String prompt =
          buildPreviewPrompt(
              designOption, pointColor, metalColor, charm, scarf, charmFile.isPresent(), scarfFile.isPresent());

      Map<String, Object> body = new LinkedHashMap<>();
      body.put("model", aiProperties.getImageModel());
      body.put("prompt", prompt);
      body.put("images", imageRefs);
      body.put("n", 1);
      body.put("size", aiProperties.getImageSize());
      body.put("quality", aiProperties.getImageQuality());
      body.put("output_format", "png");

      String raw =
          buildRestClient()
              .post()
              .uri("/images/edits")
              .body(body)
              .retrieve()
              .body(String.class);

      String b64 = extractB64(raw);
      byte[] bytes = Base64.getDecoder().decode(b64);
      return fileStorageService.uploadBytes(bytes, PREVIEW_DIR, ".png", "image/png");
    } catch (CustomException e) {
      throw e;
    } catch (RestClientException e) {
      log.error("미리보기 이미지 AI 호출 실패", e);
      throw new CustomException(GlobalErrorCode.AI_PREVIEW_FAILED);
    } catch (Exception e) {
      log.error("미리보기 이미지 처리 실패", e);
      throw new CustomException(GlobalErrorCode.AI_PREVIEW_FAILED);
    }
  }

  private StoredFile loadImage(String urlOrPath) {
    if (StringUtils.hasText(urlOrPath) && urlOrPath.contains("/uploads/")) {
      return fileStorageService.readByUrl(urlOrPath);
    }
    return staticResourceLoader
        .loadByPublicPath(urlOrPath)
        .orElseGet(() -> fileStorageService.readByUrl(urlOrPath));
  }

  private Optional<StoredFile> loadAddOnImage(AddOnProduct addOn) {
    if (addOn == null || !StringUtils.hasText(addOn.getImageUrl())) {
      return Optional.empty();
    }
    Optional<StoredFile> fromStatic = staticResourceLoader.loadByPublicPath(addOn.getImageUrl());
    if (fromStatic.isPresent()) {
      return fromStatic;
    }
    try {
      if (addOn.getImageUrl().contains("/uploads/")) {
        return Optional.of(fileStorageService.readByUrl(addOn.getImageUrl()));
      }
    } catch (Exception e) {
      log.warn("추가상품 이미지 로드 실패: {}", addOn.getImageUrl(), e);
    }
    return Optional.empty();
  }

  private Map<String, Object> toImageRef(StoredFile file) {
    String dataUrl =
        "data:"
            + file.getContentType()
            + ";base64,"
            + Base64.getEncoder().encodeToString(file.getBytes());
    Map<String, Object> imageRef = new LinkedHashMap<>();
    imageRef.put("image_url", dataUrl);
    return imageRef;
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

  private String buildPreviewPrompt(
      DesignOption designOption,
      ColorSwatchResponse pointColor,
      ColorSwatchResponse metalColor,
      AddOnProduct charm,
      AddOnProduct scarf,
      boolean hasCharmImage,
      boolean hasScarfImage) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        """
        COMPOSITE IMAGE EDIT - do NOT redesign the product.
        Image 1 is the APPROVED design mockup. Keep Image 1 almost pixel-identical: same shape, pattern, monogram, layout, camera angle.
        Only ADD small accessories / minor accent color on hardware or trims. Do not replace the main product with a new design.

        Base design name: %s
        Base design description: %s
        """
            .formatted(
                designOption.getName(),
                designOption.getDescription() == null ? "" : designOption.getDescription()));

    if (pointColor != null) {
      sb.append("Point accent color (subtle only): ")
          .append(pointColor.getLabel())
          .append(" (")
          .append(pointColor.getCode())
          .append(", ")
          .append(pointColor.getHex())
          .append("). Keep all other material colors as in Image 1.\n");
    } else {
      sb.append("Do NOT change the main product/point colors; keep Image 1 colors as-is.\n");
    }

    if (metalColor != null) {
      sb.append("Metal hardware color (existing metal parts only): ")
          .append(metalColor.getLabel())
          .append(" (")
          .append(metalColor.getCode())
          .append(").\n");
    } else {
      sb.append("Do NOT change metal hardware colors; keep Image 1 metal as-is.\n");
    }

    if (hasCharmImage) {
      sb.append(
          """
          Image 2 is the EXACT charm/keyring product photo. Attach/place it as an accessory beside or on the main product.
          Preserve Image 2 appearance faithfully - same shape, color, pattern. Do NOT restyle, redraw, or invent a different charm.
          """);
    } else if (charm != null) {
      sb.append("Add a small accessory named: ").append(charm.getName()).append(". ");
    }

    if (hasScarfImage) {
      int scarfIndex = hasCharmImage ? 3 : 2;
      sb.append("Image ")
          .append(scarfIndex)
          .append(
              """
               is the EXACT scarf product photo. Drape/tie it on or next to the main product.
              Preserve Image %d appearance faithfully - same print and colors. Do NOT redesign the scarf pattern.
              """
                  .formatted(scarfIndex));
    } else if (scarf != null) {
      sb.append("Add scarf accessory named: ").append(scarf.getName()).append(". ");
    }

    sb.append(
        "Studio product photography, clean background, no text watermark. Output one composite product photo.");
    return sb.toString();
  }

  private String extractB64(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new CustomException(GlobalErrorCode.AI_PREVIEW_FAILED);
    }
    JsonNode root = jsonMapper.readTree(rawResponse);
    JsonNode b64 = root.path("data").path(0).path("b64_json");
    if (b64.isMissingNode() || b64.isNull() || !StringUtils.hasText(b64.asText())) {
      log.warn("미리보기 응답에 b64_json 없음: {}", rawResponse);
      throw new CustomException(GlobalErrorCode.AI_PREVIEW_FAILED);
    }
    return b64.asText();
  }

  private RestClient buildRestClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(10));
    requestFactory.setReadTimeout(Duration.ofMillis(aiProperties.getImageTimeoutMs()));
    String baseUrl = aiProperties.getBaseUrl();
    if (baseUrl != null && baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
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
