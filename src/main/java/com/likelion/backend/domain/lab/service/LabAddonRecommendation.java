package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LabAddonRecommendation {

  private final Long recommendedCharmId;
  private final String recommendedCharmName;
  private final Long recommendedScarfId;
  private final String recommendedScarfName;

  public static LabAddonRecommendation empty() {
    return new LabAddonRecommendation(null, null, null, null);
  }

  public static LabAddonRecommendation of(AddOnProduct charm, AddOnProduct scarf) {
    return new LabAddonRecommendation(
        charm == null ? null : charm.getId(),
        charm == null ? null : charm.getName(),
        scarf == null ? null : scarf.getId(),
        scarf == null ? null : scarf.getName());
  }
}
