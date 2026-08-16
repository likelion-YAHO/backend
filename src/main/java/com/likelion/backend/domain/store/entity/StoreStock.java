package com.likelion.backend.domain.store.entity;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreStock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_design_id", nullable = false)
    private LabDesign labDesign;

    @Column(nullable = false)
    private Integer stockCount;

    @Builder
    public StoreStock(Store store, LabDesign labDesign, Integer stockCount) {
        this.store = store;
        this.labDesign = labDesign;
        this.stockCount = stockCount;
    }
}