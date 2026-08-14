package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.dto.LabMissionResponseDto;
import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabMissionService {

    private final LabMissionRepository labMissionRepository;

    public LabMissionResponseDto getCurrentMission() {
        LabMission mission = labMissionRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_MISSION_NOT_FOUND));

        return new LabMissionResponseDto(mission);
    }
}