package com.likelion.backend.domain.lab.dto;

import com.likelion.backend.domain.lab.entity.LabMission;
import lombok.Getter;

@Getter
public class LabMissionResponseDto {
    private Long id;
    private String targetMonth;
    private String title;
    private String description;
    private String materialDetails;

    // Entity를 받아서 DTO로 변환하는 생성자
    public LabMissionResponseDto(LabMission mission) {
        this.id = mission.getId();
        this.targetMonth = mission.getTargetMonth();
        this.title = mission.getTitle();
        this.description = mission.getDescription();
        this.materialDetails = mission.getMaterialDetails();
    }
}