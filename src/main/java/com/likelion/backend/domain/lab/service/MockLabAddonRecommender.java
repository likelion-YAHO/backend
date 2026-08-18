package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import com.likelion.backend.domain.catalog.repository.AddOnProductRepository;
import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabMission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false")
public class MockLabAddonRecommender implements LabAddonRecommender {

  private final AddOnProductRepository addOnProductRepository;

  @Override
  public LabAddonRecommendation recommend(
      BaseProduct baseProduct, String prompt, LabMission mission) {
    return LabAddonRecommendation.of(firstActive(AddOnCategory.KEYRING), firstActive(AddOnCategory.SCARF));
  }

  private AddOnProduct firstActive(AddOnCategory category) {
    List<AddOnProduct> list =
        addOnProductRepository.findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(category);
    return list.isEmpty() ? null : list.get(0);
  }
}
