package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.LabDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabDesignRepository extends JpaRepository<LabDesign, Long> {

    // 최신순 정렬 (생성일자 내림차순)
    List<LabDesign> findAllByOrderByCreatedAtDesc();

    // 인기순 정렬 (좋아요 수 내림차순)
    List<LabDesign> findAllByOrderByLikesCountDesc();

    // 실물 제작 확정된 에디션 목록 조회 (VIRTUAL 상태가 아닌 것들)
    List<LabDesign> findAllByProductionStatusNot(com.likelion.backend.domain.lab.entity.ProductionStatus status);
}