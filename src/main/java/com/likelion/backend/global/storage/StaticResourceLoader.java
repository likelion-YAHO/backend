package com.likelion.backend.global.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * classpath:/static/ 아래 정적 파일 로드 (추가상품 키링/스카프 이미지 등)
 */
@Slf4j
@Component
public class StaticResourceLoader {

  public Optional<StoredFile> loadByPublicPath(String publicPath) {
    if (!StringUtils.hasText(publicPath)) {
      return Optional.empty();
    }
    String path = publicPath.trim();
    if (path.startsWith("http://") || path.startsWith("https://")) {
      // 절대 URL이면 경로만 추출 시도
      int idx = path.indexOf("://");
      int slash = path.indexOf('/', idx + 3);
      if (slash < 0) {
        return Optional.empty();
      }
      path = path.substring(slash);
    }
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    // /uploads 는 로컬 스토리지 담당
    if (path.startsWith("/uploads/")) {
      return Optional.empty();
    }

    String classpath = "static" + path;
    ClassPathResource resource = new ClassPathResource(classpath);
    if (!resource.exists()) {
      log.warn("static 리소스 없음: {}", classpath);
      return Optional.empty();
    }
    try (InputStream in = resource.getInputStream()) {
      byte[] bytes = in.readAllBytes();
      return Optional.of(new StoredFile(bytes, probeContentType(path)));
    } catch (IOException e) {
      log.warn("static 리소스 읽기 실패: {}", classpath, e);
      return Optional.empty();
    }
  }

  private String probeContentType(String path) {
    String lower = path.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    return "image/png";
  }
}
