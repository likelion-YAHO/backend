package com.likelion.backend.domain.product.repository;

import com.likelion.backend.domain.product.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findAllByUserId(Long userId);
}
