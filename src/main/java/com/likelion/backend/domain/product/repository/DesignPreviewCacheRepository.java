package com.likelion.backend.domain.product.repository;

import com.likelion.backend.domain.product.entity.DesignPreviewCache;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignPreviewCacheRepository extends JpaRepository<DesignPreviewCache, Long> {

  Optional<DesignPreviewCache> findByCacheKey(String cacheKey);
}
