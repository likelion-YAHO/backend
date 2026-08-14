package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import lombok.Getter;

@Getter
public class LabDesignResponseDto {

    private Long id;
    private String designName;
    private ProductionStatus status;

    public LabDesignResponseDto(LabDesign labDesign) {
        this.id = labDesign.getId();
        this.designName = labDesign.getDesignName();
        this.status = labDesign.getProductionStatus();
    }
}