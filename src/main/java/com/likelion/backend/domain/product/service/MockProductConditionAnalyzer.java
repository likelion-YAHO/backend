package com.likelion.backend.domain.product.service;

import com.likelion.backend.domain.product.entity.AiCondition;
import com.likelion.backend.domain.product.entity.ProductCategory;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false")
public class MockProductConditionAnalyzer implements ProductConditionAnalyzer {

  @Override
  public ConditionAnalysisResult analyze(List<MultipartFile> images, ProductCategory category) {
    return ConditionAnalysisResult.builder()
        .condition(AiCondition.HIGH)
        .upcyclable(true)
        .recyclableParts("전면 모노그램 패널, 측면 가죽, 스트랩, 하드웨어 로고 (Mock)")
        .sizeHint(
            "카테고리 "
                + category.getLabel()
                + ", 상태 상 : 원본 크기에 맞는 중형~소형 시안 권장 (Mock)")
        .message("[Mock] 완전한 모습의 제품으로 판단되었습니다.")
        .build();
  }
}
