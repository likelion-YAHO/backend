package com.likelion.backend.domain.lab.entity;

import com.likelion.backend.domain.user.entity.User; // 유저 엔티티 경로 확인 필요!
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabDesign extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 만든 디자인인지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 미션에 출품한 건지 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private LabMission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BaseProduct baseProduct; // 커스텀 베이스 가방

    @Column(length = 100)
    private String designName;

    @Column(columnDefinition = "TEXT")
    private String concept;

    @Column 
    private String usedMaterials;

    @Column(columnDefinition = "TEXT")
    private String aiPrompt; // AI 시안 생성 시 사용했던 프롬프트

    @Column(nullable = false)
    private String imageUrl; // 최종 렌더링(생성)된 가상 이미지 URL

    @Column(nullable = false)
    private Integer likesCount = 0; // 좋아요 수 (반정규화)

    @Column(nullable = false)
    private Boolean isOfficialSelection = false; // 본사 선정 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionStatus productionStatus = ProductionStatus.VIRTUAL; // 실물 제작 상태

    private Integer price; // 선정 후 실물 제작 시 한정판 판매 가격 (NULL 허용)

    @Column(length = 50)
    private String pointColor;

    @Column(length = 50)
    private String metalColor;

    private Long charmOptionId;

    private Long scarfOptionId;

    @Column(name = "recommended_charm_id")
    private Long recommendedCharmId;

    @Column(name = "recommended_charm_name", length = 100)
    private String recommendedCharmName;

    @Column(name = "recommended_scarf_id")
    private Long recommendedScarfId;

    @Column(name = "recommended_scarf_name", length = 100)
    private String recommendedScarfName;

    @Column(nullable = false)
    private Boolean isSoldOut = false; // 품절 여부 기본값 false

    @Column(length = 50)
    private String color;

    @Column(length = 20)
    private String size;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer stock;

    @Builder
    public LabDesign(User user, LabMission mission, BaseProduct baseProduct,
                     String designName, String concept, String aiPrompt,
                     String usedMaterials, String imageUrl,
                     String pointColor, String metalColor, Long charmOptionId, Long scarfOptionId,
                     Long recommendedCharmId, String recommendedCharmName,
                     Long recommendedScarfId, String recommendedScarfName,
                     String color, String size, String description, Integer stock) {

        this.user = user;
        this.mission = mission;
        this.baseProduct = baseProduct;
        this.designName = designName;
        this.concept = concept;
        this.aiPrompt = aiPrompt;
        this.usedMaterials = usedMaterials;
        this.imageUrl = imageUrl;
        this.pointColor = pointColor;
        this.metalColor = metalColor;
        this.charmOptionId = charmOptionId;
        this.scarfOptionId = scarfOptionId;
        this.recommendedCharmId = recommendedCharmId;
        this.recommendedCharmName = recommendedCharmName;
        this.recommendedScarfId = recommendedScarfId;
        this.recommendedScarfName = recommendedScarfName;
        this.likesCount = 0;
        this.isOfficialSelection = false;
        this.productionStatus = ProductionStatus.VIRTUAL;
        this.isSoldOut = false;
        this.color = color;
        this.size = size;
        this.description = description;
        this.stock = stock;
    }

    public void markAsReadyEdition(Integer price) {
        this.isOfficialSelection = true;
        this.productionStatus = ProductionStatus.READY;
        this.price = price;
        this.isSoldOut = this.stock == null || this.stock <= 0;
    }

    public void initLikesCount(int likesCount) {
        this.likesCount = Math.max(likesCount, 0);
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void syncReadyEditionCatalog(
            String imageUrl,
            String concept,
            String color,
            String size,
            Integer stock,
            Integer price) {
        this.imageUrl = imageUrl;
        this.concept = concept;
        this.color = color;
        this.size = size;
        this.stock = stock;
        this.price = price;
        this.description = null;
        this.isSoldOut = stock == null || stock <= 0;
    }

    public void applyAddonRecommendations(
            Long charmId, String charmName, Long scarfId, String scarfName) {
        this.recommendedCharmId = charmId;
        this.recommendedCharmName = charmName;
        this.recommendedScarfId = scarfId;
        this.recommendedScarfName = scarfName;
    }

    // 좋아요 증가
    public void incrementLikeCount() {
        this.likesCount++;
    }

    // 좋아요 감소 (0 이하로 떨어지지 않게 방어)
    public void decrementLikeCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
}