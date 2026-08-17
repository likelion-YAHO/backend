package com.likelion.backend.domain.store.repository;

import com.likelion.backend.domain.store.entity.StoreStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreStockRepository extends JpaRepository<StoreStock, Long> {
    // 랩 에디션 ID로 매장 재고 리스트 찾기 (Store 정보도 같이 가져오기 위해 EntityGraph 사용)
    @EntityGraph(attributePaths = {"store"})
    List<StoreStock> findAllByLabDesignId(Long labDesignId);

    boolean existsByLabDesignId(Long labDesignId);
}