package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.LabMission;
import com.likelion.backend.domain.lab.repository.LabMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class LabMissionSeeder implements ApplicationRunner {

  private final LabMissionRepository labMissionRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (labMissionRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc().isPresent()) {
      return;
    }

    log.info("랩 미션 시드 데이터를 생성합니다.");
    labMissionRepository.save(
        LabMission.builder()
            .targetMonth("2026-09")
            .title("BOHO CHIC")
            .description(
                "MCM LAB에서 지금 나만의 가을을 커스텀하세요. "
                    + "빈티지 스웨이드를 포인트로 나만의 업사이클링 보헤미안 시크 MCM을 디자인하고 "
                    + "Lab Edition의 주인공에 도전해보세요.")
            .materialDetails("Vintage Visetos, Suede, Pink Leather")
            .isActive(true)
            .build());
  }
}
