package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.Product;
import com.likelion.backend.domain.product.entity.ProductImage;
import com.likelion.backend.global.config.AiProperties;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import com.likelion.backend.global.storage.FileStorageService;
import com.likelion.backend.global.storage.StoredFile;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 원본 제품 재활용 부위 + 상태/크기 힌트 기반 시안 추천 (완전 재생성 금지, IMAGE EDIT 유도)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiProductDesignRecommender implements ProductDesignRecommender {

  private static final int MAX_IMAGES_FOR_AI = 3;
  /** 시안 개수 : 현재는 2 -> 추후 4개로 올리기 */
  private static final int DESIGN_OPTION_COUNT = 2;
  private static final int MIN_OPTIONS = DESIGN_OPTION_COUNT;
  private static final int MAX_OPTIONS = DESIGN_OPTION_COUNT;
  private static final String DESIGN_IMAGE_DIR = "designs";

  private static final String SYSTEM_PROMPT =
      """
      당신은 MCM 업사이클 리폼 시안 디자이너입니다.
      목표는 새 제품을 상상해 그리는 것이 아니라, 사진 속 원본 제품의 가죽,원단,하드웨어를
      실제로 재활용한 결과물을 제안하는 것입니다.

      규칙:
      - 시안은 정확히 %d개
      - 입력 recyclableParts만 재료로 사용 (없는 패턴/로고 창작 금지)
      - 시안 제품 크기는 sizeHint + 원본 카테고리/크기를 "상한"으로 지킬 것
        · sizeHint 범위를 넘는 과대 시안 금지
        · "상태 하 = 무조건 키링" 같은 고정 규칙은 없음
      - 크기 다양성 (매우 중요):
        · 가능한 범위 안에서 시안들의 크기를 최대한 다양하게 배치할 것
        · 예: 소형(키링/참) ~ 중형(파우치/미니백) 등이 허용되면 한쪽에 몰지 말고 스펙트럼을 채울 것
        · 범위가 좁으면(소형만 가능) 그 안에서도 형태/용도를 다르게
        · 각 description에 대략 크기 느낌(소형/중형/준중형 등)을 한 단어로 포함
      - 각 시안 필드:
        - name: 짧은 한글 이름
        - description: 재활용 방식 + 크기 느낌 1~2문장
        - sizeLabel: 소형|중형|준중형|대형 중 하나 (sizeHint 범위 안)
        - reusedParts: recyclableParts 중 이 시안에 쓰는 부위
        - imagePrompt: 영어, "IMAGE EDIT of the provided product photo"로 시작. 완전 신규 생성 금지.

      JSON만:
      {"designs":[
        {"name":"...","description":"...","sizeLabel":"소형","reusedParts":"...","imagePrompt":"IMAGE EDIT ..."}
      ]}
      """
          .formatted(DESIGN_OPTION_COUNT);

  private final AiProperties aiProperties;
  private final JsonMapper jsonMapper;
  private final FileStorageService fileStorageService;

  @Override
  public List<DesignRecommendation> recommend(
      Product product, List<ProductImage> images, String userPrompt) {
    if (!StringUtils.hasText(aiProperties.getApiKey())) {
      throw new CustomException(GlobalErrorCode.AI_NOT_CONFIGURED);
    }
    if (images == null || images.isEmpty()) {
      throw new CustomException(GlobalErrorCode.PRODUCT_IMAGE_REQUIRED);
    }

    try {
      Map<String, Object> requestBody = buildChatRequestBody(product, images, userPrompt);
      String rawResponse =
          buildRestClient(aiProperties.getTimeoutMs())
              .post()
              .uri("/chat/completions")
              .body(requestBody)
              .retrieve()
              .body(String.class);

      List<DesignRecommendation> textDesigns = parseChatResult(rawResponse);
      String sourceImageUrl = images.get(0).getImageUrl();
      StoredFile sourceImage = fileStorageService.readByUrl(sourceImageUrl);

      List<DesignRecommendation> withImages = new ArrayList<>();
      for (DesignRecommendation design : textDesigns) {
        String imageUrl = sourceImageUrl;
        if (aiProperties.isDesignImageEnabled()) {
          try {
            imageUrl = generateAndStoreDesignImage(design, product, sourceImage, userPrompt);
          } catch (Exception e) {
            log.warn("시안 이미지 생성 실패, 제품 원본 이미지로 폴백. name={}", design.getName(), e);
          }
        }
        withImages.add(
            DesignRecommendation.builder()
                .name(design.getName())
                .description(design.getDescription())
                .imagePrompt(design.getImagePrompt())
                .imageUrl(imageUrl)
                .build());
      }
      return withImages;
    } catch (CustomException e) {
      throw e;
    } catch (RestClientException e) {
      log.error("OpenAI 시안 추천 API 호출 실패", e);
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    } catch (Exception e) {
      log.error("OpenAI 시안 추천 처리 실패", e);
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }
  }

  private String generateAndStoreDesignImage(
      DesignRecommendation design,
      Product product,
      StoredFile sourceImage,
      String userPrompt) {
    String prompt = buildImageEditPrompt(design, product, userPrompt);
    String dataUrl =
        "data:"
            + sourceImage.getContentType()
            + ";base64,"
            + Base64.getEncoder().encodeToString(sourceImage.getBytes());

    Map<String, Object> imageRef = new LinkedHashMap<>();
    imageRef.put("image_url", dataUrl);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", aiProperties.getImageModel());
    body.put("prompt", prompt);
    body.put("images", List.of(imageRef));
    body.put("n", 1);
    body.put("size", aiProperties.getImageSize());
    body.put("quality", aiProperties.getImageQuality());
    body.put("output_format", "png");

    String rawResponse =
        buildRestClient(aiProperties.getImageTimeoutMs())
            .post()
            .uri("/images/edits")
            .body(body)
            .retrieve()
            .body(String.class);

    String b64 = extractB64Image(rawResponse);
    byte[] bytes = Base64.getDecoder().decode(b64);
    return fileStorageService.uploadBytes(bytes, DESIGN_IMAGE_DIR, ".png", "image/png");
  }

  private String buildImageEditPrompt(
      DesignRecommendation design, Product product, String userPrompt) {
    String recyclable =
        StringUtils.hasText(product.getRecyclableParts())
            ? product.getRecyclableParts()
            : "usable panels and hardware from the source photo";
    String sizeHint =
        StringUtils.hasText(product.getSizeHint())
            ? product.getSizeHint()
            : "match scale to original product and damage level";
    String base =
        StringUtils.hasText(design.getImagePrompt())
            ? design.getImagePrompt()
            : "IMAGE EDIT of the provided product photo into concept: " + design.getName();

    return base
        + " CRITICAL: IMAGE EDIT only, not a brand-new generated product."
        + " Reuse ONLY materials visible in the source product photo."
        + " Recyclable parts: "
        + recyclable
        + ". Size guidance: "
        + sizeHint
        + ". Concept: "
        + design.getName()
        + ". User request: "
        + userPrompt
        + ". Category: "
        + product.getCategory().getLabel()
        + ". Photorealistic studio shot, no watermark.";
  }

  private String extractB64Image(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }
    JsonNode root = jsonMapper.readTree(rawResponse);
    JsonNode b64 = root.path("data").path(0).path("b64_json");
    if (b64.isMissingNode() || b64.isNull() || !StringUtils.hasText(b64.asText())) {
      log.warn("이미지 생성 응답에 b64_json 없음: {}", rawResponse);
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }
    return b64.asText();
  }

  private RestClient buildRestClient(long timeoutMs) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(10));
    requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

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

  private Map<String, Object> buildChatRequestBody(
      Product product, List<ProductImage> images, String userPrompt) {
    String recyclable =
        StringUtils.hasText(product.getRecyclableParts())
            ? product.getRecyclableParts()
            : "사진에 보이는 재사용 가능한 가죽/원단/하드웨어";
    String sizeHint =
        StringUtils.hasText(product.getSizeHint())
            ? product.getSizeHint()
            : "원본 카테고리·크기와 상태("
                + product.getAiCondition().getLabel()
                + ")에 맞는 시안 규모";

    List<Map<String, Object>> userContent = new ArrayList<>();
    userContent.add(
        Map.of(
            "type",
            "text",
            "text",
            """
            제품 카테고리: %s (%s)
            AI 상태: %s
            재활용 가능 부위(recyclableParts): %s
            권장 시안 규모 상한/가이드(sizeHint): %s
            사용자 디자인 가이드: %s
            시안 개수: 정확히 %d개

            recyclableParts만 재료로 쓰고, sizeHint 가능 범위 안에서
            시안 제품 크기를 최대한 다양하게 배치한 %d개를 JSON으로 반환하세요.
            (작은 것~허용된 가장 큰 것까지 스펙트럼을 채울 것. 같은 크기로 몰지 말 것)
            상태만으로 키링 고정 금지. imagePrompt는 IMAGE EDIT로 시작.
            """
                .formatted(
                    product.getCategory().getLabel(),
                    product.getCategory().name(),
                    product.getAiCondition().getLabel(),
                    recyclable,
                    sizeHint,
                    userPrompt,
                    DESIGN_OPTION_COUNT,
                    DESIGN_OPTION_COUNT)));

    int count = 0;
    for (ProductImage image : images) {
      if (count >= MAX_IMAGES_FOR_AI) {
        break;
      }
      StoredFile stored = fileStorageService.readByUrl(image.getImageUrl());
      String dataUrl =
          "data:"
              + stored.getContentType()
              + ";base64,"
              + Base64.getEncoder().encodeToString(stored.getBytes());

      Map<String, Object> imageUrl = new LinkedHashMap<>();
      imageUrl.put("url", dataUrl);
      imageUrl.put("detail", aiProperties.getDetail());

      Map<String, Object> imagePart = new LinkedHashMap<>();
      imagePart.put("type", "image_url");
      imagePart.put("image_url", imageUrl);
      userContent.add(imagePart);
      count++;
    }

    Map<String, Object> systemMessage = Map.of("role", "system", "content", SYSTEM_PROMPT);
    Map<String, Object> userMessage = new LinkedHashMap<>();
    userMessage.put("role", "user");
    userMessage.put("content", userContent);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", aiProperties.getModel());
    body.put("temperature", 0.35);
    body.put("response_format", Map.of("type", "json_object"));
    body.put("messages", List.of(systemMessage, userMessage));
    return body;
  }

  private List<DesignRecommendation> parseChatResult(String rawResponse) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }

    JsonNode root = jsonMapper.readTree(rawResponse);
    JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
    if (contentNode.isMissingNode() || contentNode.isNull()) {
      log.warn("OpenAI 시안 응답에 content 없음: {}", rawResponse);
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }

    String content = stripCodeFence(contentNode.asText());
    JsonNode result = jsonMapper.readTree(content);
    JsonNode designs = result.path("designs");
    if (!designs.isArray() || designs.isEmpty()) {
      designs = result.isArray() ? result : designs;
    }
    if (!designs.isArray() || designs.isEmpty()) {
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }

    List<DesignRecommendation> recommendations = new ArrayList<>();
    for (JsonNode node : designs) {
      String name = textOrEmpty(node, "name");
      String description = textOrEmpty(node, "description");
      String sizeLabel = textOrEmpty(node, "sizeLabel");
      if (!StringUtils.hasText(sizeLabel)) {
        sizeLabel = textOrEmpty(node, "size_label");
      }
      String reusedParts = textOrEmpty(node, "reusedParts");
      if (!StringUtils.hasText(reusedParts)) {
        reusedParts = textOrEmpty(node, "reused_parts");
      }
      String imagePrompt = textOrEmpty(node, "imagePrompt");
      if (!StringUtils.hasText(imagePrompt)) {
        imagePrompt = textOrEmpty(node, "image_prompt");
      }
      if (!StringUtils.hasText(name)) {
        continue;
      }

      if (StringUtils.hasText(sizeLabel)) {
        description =
            StringUtils.hasText(description)
                ? "[" + sizeLabel + "] " + description
                : "크기: " + sizeLabel;
      }
      if (StringUtils.hasText(reusedParts) && StringUtils.hasText(description)) {
        description = description + " [재활용: " + reusedParts + "]";
      } else if (StringUtils.hasText(reusedParts)) {
        description =
            StringUtils.hasText(description)
                ? description + " [재활용: " + reusedParts + "]"
                : "재활용 부위: " + reusedParts;
      }

      recommendations.add(
          DesignRecommendation.builder()
              .name(name.trim())
              .description(StringUtils.hasText(description) ? description.trim() : null)
              .imagePrompt(StringUtils.hasText(imagePrompt) ? imagePrompt.trim() : null)
              .build());
      if (recommendations.size() >= MAX_OPTIONS) {
        break;
      }
    }

    if (recommendations.size() < MIN_OPTIONS) {
      log.warn("시안 개수 부족: {}", recommendations.size());
      throw new CustomException(GlobalErrorCode.AI_DESIGN_FAILED);
    }
    return recommendations;
  }

  private static String textOrEmpty(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? "" : value.asText();
  }

  private static String stripCodeFence(String content) {
    String trimmed = content.trim();
    if (trimmed.startsWith("```")) {
      int firstNewline = trimmed.indexOf('\n');
      if (firstNewline > 0) {
        trimmed = trimmed.substring(firstNewline + 1);
      }
      if (trimmed.endsWith("```")) {
        trimmed = trimmed.substring(0, trimmed.length() - 3);
      }
    }
    return trimmed.trim();
  }
}
