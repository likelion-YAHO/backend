package com.likelion.backend.domain.lab.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "lab_design_preview_caches",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_lab_design_preview_cache_key", columnNames = "cache_key"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LabDesignPreviewCache extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "source_image_url", nullable = false, length = 512)
  private String sourceImageUrl;

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
