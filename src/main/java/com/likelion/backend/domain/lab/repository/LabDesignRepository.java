package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LabDesignRepository extends JpaRepository<LabDesign, Long> {

    // 최신순 정렬 (생성일자 내림차순)
    List<LabDesign> findAllByOrderByCreatedAtDesc();

    // 인기순 정렬 (좋아요 수 내림차순)
    List<LabDesign> findAllByOrderByLikesCountDesc();

    List<LabDesign> findAllByProductionStatusOrderByCreatedAtDesc(ProductionStatus status);

    List<LabDesign> findAllByProductionStatusOrderByLikesCountDesc(ProductionStatus status);

    // 실물 제작 확정된 에디션 목록 조회 (VIRTUAL 상태가 아닌 것들)
    List<LabDesign> findAllByProductionStatusNot(ProductionStatus status);

    Optional<LabDesign> findFirstByDesignNameAndProductionStatusNot(
            String designName, ProductionStatus status);

    boolean existsByUser_IdAndProductionStatus(Long userId, ProductionStatus status);

    Optional<LabDesign> findFirstByUser_IdAndProductionStatus(
            Long userId, ProductionStatus status);
}