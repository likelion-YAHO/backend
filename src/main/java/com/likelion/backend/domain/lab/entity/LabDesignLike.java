package com.likelion.backend.domain.lab.entity;

import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabDesignLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_design_id", nullable = false)
    private LabDesign labDesign;

    @Builder
    public LabDesignLike(User user, LabDesign labDesign) {
        this.user = user;
        this.labDesign = labDesign;
    }
}