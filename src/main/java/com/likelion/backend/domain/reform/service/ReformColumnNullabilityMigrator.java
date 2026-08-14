package com.likelion.backend.domain.reform.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ReformColumnNullabilityMigrator implements ApplicationRunner {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // 초기 엔티티는 point/metal NOT NULL. ddl-auto=update는 기존 컬럼 null 허용을 안 바꿈
    entityManager
        .createNativeQuery("ALTER TABLE reforms MODIFY COLUMN point_color VARCHAR(50) NULL")
        .executeUpdate();
    entityManager
        .createNativeQuery("ALTER TABLE reforms MODIFY COLUMN metal_color VARCHAR(50) NULL")
        .executeUpdate();
    log.info("reforms.point_color / metal_color 를 NULL 허용으로 맞췄습니다.");
  }
}
