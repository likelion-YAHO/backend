package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.LabMission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LabMissionRepository extends JpaRepository<LabMission, Long> {
    // 현재 진행 중인(isActive = true) 미션 중 가장 최근에 생성된 1개 조회
    Optional<LabMission> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}