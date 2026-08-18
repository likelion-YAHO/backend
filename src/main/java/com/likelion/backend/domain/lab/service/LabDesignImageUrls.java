package com.likelion.backend.domain.lab.service;

import org.springframework.util.StringUtils;

/**
 * LAB generate/preview 산출물은 /uploads/lab-designs|lab-previews/ 아래만 허용
 */
public final class LabDesignImageUrls {

  static final String LAB_DESIGNS_DIR = "lab-designs";
  static final String LAB_PREVIEWS_DIR = "lab-previews";

  private static final String UPLOADS_PREFIX = "/uploads/";
  private static final String FILE_NAME = "[A-Za-z0-9._-]+";

  private LabDesignImageUrls() {}

  public static boolean isLabDesignSource(String url) {
    return hasSafeUploadFile(url, LAB_DESIGNS_DIR);
  }

  public static boolean isLabManagedImage(String url) {
    return hasSafeUploadFile(url, LAB_DESIGNS_DIR) || hasSafeUploadFile(url, LAB_PREVIEWS_DIR);
  }

  /**
   * 스토리지가 읽는 절대 URL로 맞춘다. 이미 baseUrl 이면 그대로, /uploads/... 상대경로면 baseUrl에 붙인다.
   */
  public static String toStorageUrl(String url, String storageBaseUrl) {
    if (!StringUtils.hasText(url) || !StringUtils.hasText(storageBaseUrl)) {
      return url;
    }
    String trimmed = url.trim();
    String base = trimTrailingSlash(storageBaseUrl.trim());
    if (trimmed.startsWith(base + "/")) {
      return trimmed;
    }
    int uploadsIdx = trimmed.indexOf(UPLOADS_PREFIX);
    if (uploadsIdx < 0) {
      return trimmed;
    }
    return base + trimmed.substring(uploadsIdx + "/uploads".length());
  }

  static boolean hasSafeUploadFile(String url, String directory) {
    if (!StringUtils.hasText(url) || !StringUtils.hasText(directory)) {
      return false;
    }
    String marker = UPLOADS_PREFIX + directory + "/";
    int idx = url.trim().indexOf(marker);
    if (idx < 0) {
      return false;
    }
    String file = url.trim().substring(idx + marker.length());
    if (!StringUtils.hasText(file)
        || file.contains("/")
        || file.contains("\\")
        || file.contains("..")) {
      return false;
    }
    return file.matches(FILE_NAME);
  }

  private static String trimTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
