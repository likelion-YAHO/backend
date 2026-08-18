package com.likelion.backend.domain.preview;

import com.likelion.backend.domain.catalog.dto.ColorSwatchResponse;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreviewComposeCommand {

  private final String sourceImageUrl;
  private final String uploadDirectory;
  private final String baseDesignName;
  private final String baseDesignDescription;
  private final ColorSwatchResponse pointColor;
  private final ColorSwatchResponse metalColor;
  private final AddOnProduct charm;
  private final AddOnProduct scarf;
}
