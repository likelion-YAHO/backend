package com.likelion.backend.domain.catalog.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "add_on_products")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddOnProduct extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AddOnCategory category;

  @Column(nullable = false)
  private int price;

  @Column(name = "image_url", length = 512)
  private String imageUrl;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Builder.Default
  @Column(name = "sort_order", nullable = false)
  private int sortOrder = 0;

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
