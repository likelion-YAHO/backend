package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.LabDesignLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabDesignLikeRepository extends JpaRepository<LabDesignLike, Long> {

    // 특정 유저가 특정 디자인에 누른 좋아요 기록 찾기
    Optional<LabDesignLike> findByUserIdAndLabDesignId(Long userId, Long labDesignId);

    // 특정 유저가 누른 모든 좋아요 기록 가져오기
    List<LabDesignLike> findAllByUserId(Long userId);

    void deleteAllByLabDesignId(Long labDesignId);
}