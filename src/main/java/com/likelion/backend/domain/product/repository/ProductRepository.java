package com.likelion.backend.domain.product.repository;

import com.likelion.backend.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

  Optional<Product> findByIdAndUser_Id(Long id, Long userId);
}
