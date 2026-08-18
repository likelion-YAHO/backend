package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.service.CatalogService;
import com.likelion.backend.domain.lab.dto.LabDesignPreviewRequest;
import com.likelion.backend.domain.lab.dto.LabDesignPreviewResponse;
import com.likelion.backend.domain.lab.entity.LabDesignPreviewCache;
import com.likelion.backend.domain.lab.repository.LabDesignPreviewCacheRepository;
import com.likelion.backend.domain.preview.AddonCompositePreviewService;
import com.likelion.backend.domain.preview.PreviewComposeCommand;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabDesignPreviewService {

  private static final String NONE = "NONE";

  private final CatalogService catalogService;
  private final LabDesignPreviewCacheRepository labDesignPreviewCacheRepository;
  private final AddonCompositePreviewService addonCompositePreviewService;

  @Value("${app.storage.local.base-url}")
  private String storageBaseUrl;

  @Transactional
  public LabDesignPreviewResponse createPreview(LabDesignPreviewRequest request) {
    if (!LabDesignImageUrls.isLabDesignSource(request.getSourceImageUrl())) {
      throw new CustomException(GlobalErrorCode.LAB_PREVIEW_SOURCE_INVALID);
    }
    String sourceImageUrl =
        LabDesignImageUrls.toStorageUrl(request.getSourceImageUrl(), storageBaseUrl);

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
    String cacheKey = buildCacheKey(sourceImageUrl, pointKey, metalKey, charmKey, scarfKey);

    var cached = labDesignPreviewCacheRepository.findByCacheKey(cacheKey);
    if (cached.isPresent()) {
      return toResponse(
          true,
          cached.get().getImageUrl(),
          cacheKey,
          sourceImageUrl,
          pointColor == null ? null : pointColor.getCode(),
          metalColor == null ? null : metalColor.getCode(),
          charm,
          scarf);
    }

    String imageUrl =
        addonCompositePreviewService.compose(
            PreviewComposeCommand.builder()
                .sourceImageUrl(sourceImageUrl)
                .uploadDirectory(LabDesignImageUrls.LAB_PREVIEWS_DIR)
                .baseDesignName("MCM LAB design")
                .baseDesignDescription("")
                .pointColor(pointColor)
                .metalColor(metalColor)
                .charm(charm)
                .scarf(scarf)
                .build());

    LabDesignPreviewCache saved =
        labDesignPreviewCacheRepository.save(
            LabDesignPreviewCache.builder()
                .sourceImageUrl(sourceImageUrl)
                .pointColor(pointKey)
                .metalColor(metalKey)
                .charmOptionId(charmKey)
                .scarfOptionId(scarfKey)
                .cacheKey(cacheKey)
                .imageUrl(imageUrl)
                .build());

    return toResponse(
        false,
        saved.getImageUrl(),
        cacheKey,
        sourceImageUrl,
        pointColor == null ? null : pointColor.getCode(),
        metalColor == null ? null : metalColor.getCode(),
        charm,
        scarf);
  }

  public static String buildCacheKey(
      String sourceImageUrl,
      String pointColor,
      String metalColor,
      String charmKey,
      String scarfKey) {
    return String.join(
        ":",
        "lab",
        shortHash(sourceImageUrl),
        pointColor.toUpperCase(Locale.ROOT),
        metalColor.toUpperCase(Locale.ROOT),
        charmKey,
        scarfKey);
  }

  static String shortHash(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest).substring(0, 16);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private LabDesignPreviewResponse toResponse(
      boolean cacheHit,
      String imageUrl,
      String cacheKey,
      String sourceImageUrl,
      String pointColor,
      String metalColor,
      AddOnProduct charm,
      AddOnProduct scarf) {
    return LabDesignPreviewResponse.builder()
        .cacheHit(cacheHit)
        .previewImageUrl(imageUrl)
        .cacheKey(cacheKey)
        .sourceImageUrl(sourceImageUrl)
        .pointColor(pointColor)
        .metalColor(metalColor)
        .charmOptionId(charm == null ? null : charm.getId())
        .scarfOptionId(scarf == null ? null : scarf.getId())
        .charmOptionName(charm == null ? null : charm.getName())
        .scarfOptionName(scarf == null ? null : scarf.getName())
        .build();
  }
}
