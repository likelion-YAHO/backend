package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import com.likelion.backend.domain.lab.entity.LabAiGenerationAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabAiGenerationAttemptRepository
    extends JpaRepository<LabAiGenerationAttempt, Long> {

  Optional<LabAiGenerationAttempt> findByUser_IdAndMission_IdAndBaseProduct(
      Long userId, Long missionId, BaseProduct baseProduct);

  void deleteByUser_IdAndMission_IdAndBaseProduct(
      Long userId, Long missionId, BaseProduct baseProduct);
}
