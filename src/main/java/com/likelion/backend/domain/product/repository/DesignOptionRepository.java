package com.likelion.backend.domain.product.repository;

import com.likelion.backend.domain.product.entity.DesignOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignOptionRepository extends JpaRepository<DesignOption, Long> {

  List<DesignOption> findAllByProductIdOrderBySortOrderAsc(Long productId);

  void deleteAllByProductId(Long productId);
}
