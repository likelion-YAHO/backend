package com.likelion.backend.domain.product.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "design_preview_caches",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_design_preview_cache_key", columnNames = "cache_key"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DesignPreviewCache extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "design_option_id", nullable = false)
  private Long designOptionId;

  @Column(name = "point_color", nullable = false, length = 50)
  private String pointColor;

  @Column(name = "metal_color", nullable = false, length = 50)
  private String metalColor;

  @Column(name = "charm_option_id", nullable = false, length = 30)
  private String charmOptionId;

  @Column(name = "scarf_option_id", nullable = false, length = 30)
  private String scarfOptionId;

  @Column(name = "cache_key", nullable = false, length = 200, unique = true)
  private String cacheKey;

  @Column(name = "image_url", nullable = false, length = 512)
  private String imageUrl;
}
