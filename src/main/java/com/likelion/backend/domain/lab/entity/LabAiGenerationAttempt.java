package com.likelion.backend.domain.lab.entity;

import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "lab_ai_generation_attempts",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_lab_ai_gen_user_mission_product",
            columnNames = {"user_id", "mission_id", "base_product"}))
public class LabAiGenerationAttempt extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "mission_id", nullable = false)
  private LabMission mission;

  @Enumerated(EnumType.STRING)
  @Column(name = "base_product", nullable = false, length = 50)
  private BaseProduct baseProduct;

  @Column(name = "try_count", nullable = false)
  private int tryCount;

  @Column(name = "last_image_url", length = 512)
  private String lastImageUrl;

  @Column(name = "recommended_charm_id")
  private Long recommendedCharmId;

  @Column(name = "recommended_charm_name", length = 100)
  private String recommendedCharmName;

  @Column(name = "recommended_scarf_id")
  private Long recommendedScarfId;

  @Column(name = "recommended_scarf_name", length = 100)
  private String recommendedScarfName;

  @Builder
  public LabAiGenerationAttempt(User user, LabMission mission, BaseProduct baseProduct) {
    this.user = user;
    this.mission = mission;
    this.baseProduct = baseProduct;
    this.tryCount = 0;
  }

  public void incrementTryCount() {
    this.tryCount++;
  }

  public void recordGeneration(
      String imageUrl,
      Long charmId,
      String charmName,
      Long scarfId,
      String scarfName) {
    this.lastImageUrl = imageUrl;
    this.recommendedCharmId = charmId;
    this.recommendedCharmName = charmName;
    this.recommendedScarfId = scarfId;
    this.recommendedScarfName = scarfName;
  }
}
