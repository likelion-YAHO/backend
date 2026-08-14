package com.likelion.backend.domain.store.repository;

import com.likelion.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
