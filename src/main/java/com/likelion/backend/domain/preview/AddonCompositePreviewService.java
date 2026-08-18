package com.likelion.backend.domain.preview;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
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
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddonCompositePreviewService {

  private final FileStorageService fileStorageService;
  private final StaticResourceLoader staticResourceLoader;
  private final AiProperties aiProperties;
  private final JsonMapper jsonMapper;

  public String compose(PreviewComposeCommand command) {
    String sourceImageUrl = command.getSourceImageUrl();
    if (!aiProperties.isEnabled()
        || !aiProperties.isDesignImageEnabled()
        || !StringUtils.hasText(aiProperties.getApiKey())) {
      log.info("미리보기 AI 생략(시안 이미지 사용). source={}", sourceImageUrl);
      return sourceImageUrl;
    }

    try {
      StoredFile baseDesign = loadImage(sourceImageUrl);
      List<Map<String, Object>> imageRefs = new ArrayList<>();
      imageRefs.add(toImageRef(baseDesign));

      Optional<StoredFile> charmFile = loadAddOnImage(command.getCharm());
      Optional<StoredFile> scarfFile = loadAddOnImage(command.getScarf());
      charmFile.ifPresent(f -> imageRefs.add(toImageRef(f)));
      scarfFile.ifPresent(f -> imageRefs.add(toImageRef(f)));

      String prompt =
          buildPreviewPrompt(
              command.getBaseDesignName(),
              command.getBaseDesignDescription(),
              command.getPointColor(),
              command.getMetalColor(),
              command.getCharm(),
              command.getScarf(),
              charmFile.isPresent(),
              scarfFile.isPresent());

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
      return fileStorageService.uploadBytes(
          bytes, command.getUploadDirectory(), ".png", "image/png");
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

  private String buildPreviewPrompt(
      String baseDesignName,
      String baseDesignDescription,
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
                baseDesignName == null ? "" : baseDesignName,
                baseDesignDescription == null ? "" : baseDesignDescription));

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
}
