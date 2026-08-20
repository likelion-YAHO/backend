package com.likelion.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

  /** true면 OpenAI, false면 Mock */
  private boolean enabled = true;

  private String baseUrl = "https://api.openai.com/v1";

  private String apiKey = "";

  private String model = "gpt-4.1-mini";

  /** low | high | auto */
  private String detail = "low";

  private long timeoutMs = 60_000L;

  /** 시안 이미지 생성 여부 */
  private boolean designImageEnabled = true;

  /** 시안 이미지 모델 */
  private String imageModel = "gpt-image-1.5";

  /** low | medium | high | auto */
  private String imageQuality = "low";

  private String imageSize = "1024x1024";

  private long imageTimeoutMs = 120_000L;
}
