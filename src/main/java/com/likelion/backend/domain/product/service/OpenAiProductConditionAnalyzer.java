package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.AiCondition;
import com.likelion.backend.domain.product.entity.ProductCategory;
import com.likelion.backend.global.config.AiProperties;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OpenAI Vision으로 제품 상태(상/중/하), 업사이클 가능 여부, 재활용 부위, 권장 시안 규모를 판정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiProductConditionAnalyzer implements ProductConditionAnalyzer {

  private static final String SYSTEM_PROMPT =
      """
      당신은 MCM 제품 업사이클 검수 AI입니다.
      업로드된 제품 사진과 카테고리를 보고 훼손 상태, 업사이클 가능 여부, 재활용 가능 부위,
      그리고 추천 가능한 시안 제품의 대략적 크기를 판정합니다.

      판정 기준 (상태):
      - 상: 형태 완전, 큰 훼손·오염 거의 없음
      - 중: 사용감·약한 스크래치·부분 오염, 주요 패널 일부 재사용 가능
      - 하: 심한 찢김·오염·형태 붕괴, 사용 가능한 조각이 제한적

      upcyclable:
      - 재사용 가능한 소재가 조금이라도 있으면 true
      - 완전히 소실·오염으로 재료로 쓸 수 없으면 false

      sizeHint (중요 — KEYRING 고정이 아님):
      - 원본 제품 크기/카테고리 + 상태를 함께 고려해 "가능한 시안 크기 범위"를 문장으로 적으세요
      - 단일 크기가 아니라 가능하면 하한~상한 스펙트럼을 쓰세요
        예: "소형 키링부터 중형 파우치까지 가능"
        예: "원본 미니백·상태 상 → 소형 위주, 최대 미니 토트"
        예: "대형 백팩·상태 하 → 남은 패널로 소형~준중형 소품"
      - "상태 하 = 무조건 키링" 고정 규칙 금지

      recyclableParts: 사진에서 실제로 재사용 가능해 보이는 부위·소재를 구체적으로

      반드시 아래 JSON만 출력 (마크다운 금지):
      {
        "condition":"상",
        "upcyclable":true,
        "recyclableParts":"전면 모노그램 패널, 측면 스트랩, 골드 하드웨어",
        "sizeHint":"원본이 백팩 크기이고 상태 상이라 중형 파우치~키링까지 가능",
        "message":"한 줄 한국어 설명"
      }
      condition: 상|중|하
      """;

  private final AiProperties aiProperties;
  private final JsonMapper jsonMapper;

  @Override
  public ConditionAnalysisResult analyze(List<MultipartFile> images, ProductCategory category) {
    if (!StringUtils.hasText(aiProperties.getApiKey())) {
      throw new CustomException(GlobalErrorCode.AI_NOT_CONFIGURED);
    }

    try {
      Map<String, Object> requestBody = buildRequestBody(images, category);
      String rawResponse =
          buildRestClient()
              .post()
              .uri("/chat/completions")
              .body(requestBody)
              .retrieve()
              .body(String.class);

      return parseResult(rawResponse, category);
    } catch (CustomException e) {
      throw e;
    } catch (RestClientException e) {
      log.error("OpenAI API 호출 실패", e);
      throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
    } catch (Exception e) {
      log.error("OpenAI 응답 처리 실패", e);
      throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
    }
  }

  private RestClient buildRestClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(10));
    requestFactory.setReadTimeout(Duration.ofMillis(aiProperties.getTimeoutMs()));

    String baseUrl = trimTrailingSlash(aiProperties.getBaseUrl());
    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  private Map<String, Object> buildRequestBody(List<MultipartFile> images, ProductCategory category)
      throws Exception {
    List<Map<String, Object>> userContent = new ArrayList<>();
    userContent.add(
        Map.of(
            "type",
            "text",
            "text",
            "제품 카테고리: "
                + category.getLabel()
                + " ("
                + category.name()
                + ")\n"
                + "상태·upcyclable·recyclableParts·sizeHint를 판정하고 JSON만 반환하세요.\n"
                + "sizeHint는 손상도만이 아니라 이 카테고리/원본 크기를 반영하세요."));

    for (MultipartFile image : images) {
      String dataUrl = toDataUrl(image);
      Map<String, Object> imageUrl = new LinkedHashMap<>();
      imageUrl.put("url", dataUrl);
      imageUrl.put("detail", aiProperties.getDetail());

      Map<String, Object> imagePart = new LinkedHashMap<>();
      imagePart.put("type", "image_url");
      imagePart.put("image_url", imageUrl);
      userContent.add(imagePart);
    }

    Map<String, Object> systemMessage = Map.of("role", "system", "content", SYSTEM_PROMPT);
    Map<String, Object> userMessage = new LinkedHashMap<>();
    userMessage.put("role", "user");
    userMessage.put("content", userContent);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", aiProperties.getModel());
    body.put("temperature", 0.1);
    body.put("response_format", Map.of("type", "json_object"));
    body.put("messages", List.of(systemMessage, userMessage));
    return body;
  }

  private String toDataUrl(MultipartFile image) throws Exception {
    String contentType = image.getContentType();
    if (!StringUtils.hasText(contentType)) {
      contentType = MediaType.IMAGE_JPEG_VALUE;
    }
    String base64 = Base64.getEncoder().encodeToString(image.getBytes());
    return "data:" + contentType + ";base64," + base64;
  }

  private ConditionAnalysisResult parseResult(String rawResponse, ProductCategory category) {
    if (!StringUtils.hasText(rawResponse)) {
      throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
    }

    JsonNode root = jsonMapper.readTree(rawResponse);
    JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
    if (contentNode.isMissingNode() || contentNode.isNull()) {
      log.warn("OpenAI 응답에 content 없음: {}", rawResponse);
      throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
    }

    String content = stripCodeFence(contentNode.asText());
    JsonNode result = jsonMapper.readTree(content);

    AiCondition condition = parseCondition(textOrEmpty(result, "condition"));
    boolean upcyclable = result.path("upcyclable").asBoolean(true);

    String recyclableParts = textOrEmpty(result, "recyclableParts");
    if (!StringUtils.hasText(recyclableParts)) {
      recyclableParts = textOrEmpty(result, "recyclable_parts");
    }
    if (!StringUtils.hasText(recyclableParts) && upcyclable) {
      recyclableParts = "사진에 보이는 재사용 가능한 가죽/원단/하드웨어";
    }

    String sizeHint = textOrEmpty(result, "sizeHint");
    if (!StringUtils.hasText(sizeHint)) {
      sizeHint = textOrEmpty(result, "size_hint");
    }
    if (!StringUtils.hasText(sizeHint)) {
      sizeHint =
          "카테고리 "
              + category.getLabel()
              + ", 상태 "
              + condition.getLabel()
              + " 기준으로 원본 크기에 맞는 시안 규모를 추천";
    }

    String message = textOrEmpty(result, "message");
    if (!StringUtils.hasText(message)) {
      message =
          upcyclable
              ? "AI가 제품 상태를 '" + condition.getLabel() + "'(으)로 판정했습니다."
              : "업사이클에 적합한 재료를 찾기 어렵습니다.";
    }

    return ConditionAnalysisResult.builder()
        .condition(condition)
        .upcyclable(upcyclable)
        .message(message)
        .recyclableParts(recyclableParts)
        .sizeHint(sizeHint)
        .build();
  }

  private AiCondition parseCondition(String raw) {
    if (!StringUtils.hasText(raw)) {
      throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "상", "high", "good", "excellent" -> AiCondition.HIGH;
      case "중", "medium", "mid", "fair", "normal" -> AiCondition.MEDIUM;
      case "하", "low", "poor", "bad" -> AiCondition.LOW;
      default -> {
        try {
          yield AiCondition.fromLabel(raw.trim());
        } catch (IllegalArgumentException e) {
          log.warn("알 수 없는 condition 값: {}", raw);
          throw new CustomException(GlobalErrorCode.AI_ANALYSIS_FAILED);
        }
      }
    };
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

  private static String trimTrailingSlash(String url) {
    if (url == null) {
      return "";
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}
