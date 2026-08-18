package com.likelion.backend.domain.lab.repository;

import com.likelion.backend.domain.lab.entity.LabDesignPreviewCache;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabDesignPreviewCacheRepository
    extends JpaRepository<LabDesignPreviewCache, Long> {

  Optional<LabDesignPreviewCache> findByCacheKey(String cacheKey);
}
