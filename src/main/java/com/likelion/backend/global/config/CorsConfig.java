package com.likelion.backend.global.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Value("${app.cors.allowed-origins}")
  private String[] allowedOrigins;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // yml 출처. https://*.netlify.app 같은 패턴은 Origins가 아니라 OriginPatterns
    config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));

    // 허용할 HTTP 메서드
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // 허용할 요청 Header
    config.setAllowedHeaders(List.of("*"));

    // 프론트에서 JWT 토큰(Authorization)을 읽을 수 있도록 허용
    config.setExposedHeaders(List.of("Authorization"));

    // 쿠키 및 인증 정보를 포함한 요청 허용
    config.setAllowCredentials(true);

    // 브라우저의 CORS 검사 결과 캐싱 시간 설정
    config.setMaxAge(3600L);

    // 모든 API 경로(/**)에 이 CORS 설정을 적용
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
