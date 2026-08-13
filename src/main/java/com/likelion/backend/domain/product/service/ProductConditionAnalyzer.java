package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.ProductCategory;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 제품 이미지 기반 훼손 상태(상/중/하) Mock 분석 포트
 */
public interface ProductConditionAnalyzer {

  ConditionAnalysisResult analyze(List<MultipartFile> images, ProductCategory category);
}
