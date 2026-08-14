package com.likelion.backend.domain.lab.entity;

import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String targetMonth; // 예: "2026-08"

    @Column(nullable = false, length = 100)
    private String title; // 예: "RE:VISIT"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // 미션 상세 설명

    @Column(nullable = false, columnDefinition = "TEXT")
    private String materialDetails; // 제공 소재 내역

    @Column(nullable = false)
    private Boolean isActive = true; // 현재 진행 중인 미션인지 여부

    @Builder
    public LabMission(String targetMonth, String title, String description, String materialDetails, Boolean isActive) {
        this.targetMonth = targetMonth;
        this.title = title;
        this.description = description;
        this.materialDetails = materialDetails;
        this.isActive = isActive != null ? isActive : true;
    }
}