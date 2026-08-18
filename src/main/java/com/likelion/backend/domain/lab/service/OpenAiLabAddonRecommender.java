package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.repository.AddOnProductRepository;
import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.global.config.AiProperties;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiLabAddonRecommender implements LabAddonRecommender {

  private static final String SYSTEM_PROMPT =
      """
      당신은 MCM LAB 커스텀 스타일리스트입니다.
      제공된 KEYRING 후보와 SCARF 후보에서 각 1개를 고르세요.
      목록에 없는 id는 금지합니다.
      JSON만 반환하세요:
      {"recommendedCharmId":1,"recommendedScarfId":5}
      """;

  private final AddOnProductRepository addOnProductRepository;
  private final AiProperties aiProperties;
  private final JsonMapper jsonMapper;

  @Override
  public LabAddonRecommendation recommend(
      BaseProduct baseProduct, String prompt, LabMission mission) {
    List<AddOnProduct> keyrings =
        addOnProductRepository.findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(
            AddOnCategory.KEYRING);
    List<AddOnProduct> scarves =
        addOnProductRepository.findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(
            AddOnCategory.SCARF);
    if (keyrings.isEmpty() && scarves.isEmpty()) {
      return LabAddonRecommendation.empty();
    }
    if (!StringUtils.hasText(aiProperties.getApiKey())) {
      return LabAddonRecommendation.of(first(keyrings), first(scarves));
    }

    try {
      String raw =
          buildRestClient()
              .post()
              .uri("/chat/completions")
              .body(buildRequestBody(baseProduct, prompt, mission, keyrings, scarves))
              .retrieve()
              .body(String.class);
      return parse(raw, keyrings, scarves);
    } catch (Exception e) {
      log.warn("LAB 추가상품 추천 실패, 카탈로그 첫 상품으로 폴백", e);
      return LabAddonRecommendation.of(first(keyrings), first(scarves));
    }
  }

  private Map<String, Object> buildRequestBody(
      BaseProduct baseProduct,
      String prompt,
      LabMission mission,
      List<AddOnProduct> keyrings,
      List<AddOnProduct> scarves) {
    String missionTitle = mission == null ? "" : mission.getTitle();
    String missionMaterials = mission == null ? "" : mission.getMaterialDetails();
    String productName = baseProduct == null ? "" : baseProduct.getProductName();

    Map<String, Object> userMessage = new LinkedHashMap<>();
    userMessage.put("role", "user");
    userMessage.put(
        "content",
        """
        베이스 제품: %s
        미션 테마: %s
        제공 소재: %s
        사용자 디자인 가이드: %s

        KEYRING 후보 (recommendedCharmId는 이 중 하나):
        %s

        SCARF 후보 (recommendedScarfId는 이 중 하나):
        %s
        """
            .formatted(
                productName,
                missionTitle,
                missionMaterials,
                prompt == null ? "" : prompt,
                formatCatalog(keyrings),
                formatCatalog(scarves)));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", aiProperties.getModel());
    body.put("temperature", 0.3);
    body.put("response_format", Map.of("type", "json_object"));
    body.put("messages", List.of(Map.of("role", "system", "content", SYSTEM_PROMPT), userMessage));
    return body;
  }

  private LabAddonRecommendation parse(
      String rawResponse, List<AddOnProduct> keyrings, List<AddOnProduct> scarves) {
    if (!StringUtils.hasText(rawResponse)) {
      return LabAddonRecommendation.of(first(keyrings), first(scarves));
    }
    JsonNode root = jsonMapper.readTree(rawResponse);
    JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
    if (contentNode.isMissingNode() || contentNode.isNull()) {
      return LabAddonRecommendation.of(first(keyrings), first(scarves));
    }
    JsonNode result = jsonMapper.readTree(stripCodeFence(contentNode.asText()));
    AddOnProduct charm =
        resolveById(keyrings, readId(result, "recommendedCharmId", "recommended_charm_id"));
    AddOnProduct scarf =
        resolveById(scarves, readId(result, "recommendedScarfId", "recommended_scarf_id"));
    return LabAddonRecommendation.of(charm, scarf);
  }

  private static String formatCatalog(List<AddOnProduct> products) {
    if (products == null || products.isEmpty()) {
      return "(없음)";
    }
    StringBuilder sb = new StringBuilder();
    for (AddOnProduct product : products) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append("- id=").append(product.getId()).append(", name=").append(product.getName());
    }
    return sb.toString();
  }

  private static Long readId(JsonNode node, String camel, String snake) {
    JsonNode value = node.path(camel);
    if (value.isMissingNode() || value.isNull()) {
      value = node.path(snake);
    }
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    if (value.isNumber()) {
      return value.asLong();
    }
    String text = value.asText();
    if (!StringUtils.hasText(text)) {
      return null;
    }
    try {
      return Long.parseLong(text.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static AddOnProduct resolveById(List<AddOnProduct> catalog, Long id) {
    if (catalog == null || catalog.isEmpty()) {
      return null;
    }
    if (id != null) {
      Map<Long, AddOnProduct> byId =
          catalog.stream()
              .collect(Collectors.toMap(AddOnProduct::getId, Function.identity(), (a, b) -> a));
      AddOnProduct found = byId.get(id);
      if (found != null) {
        return found;
      }
    }
    return catalog.get(0);
  }

  private static AddOnProduct first(List<AddOnProduct> catalog) {
    return catalog == null || catalog.isEmpty() ? null : catalog.get(0);
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

  private RestClient buildRestClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(10));
    requestFactory.setReadTimeout(Duration.ofMillis(aiProperties.getTimeoutMs()));
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
