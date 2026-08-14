package com.likelion.backend.domain.reform.repository;

import com.likelion.backend.domain.reform.entity.Reform;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReformRepository extends JpaRepository<Reform, Long> {

  /** 제품당 현재 선택 완료 1건 */
  Optional<Reform> findByProduct_Id(Long productId);

  Optional<Reform> findByIdAndProduct_User_Id(Long id, Long userId);
}
