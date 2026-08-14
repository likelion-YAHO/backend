package com.likelion.backend.domain.catalog.repository;

import com.likelion.backend.domain.catalog.entity.AddOnCategory;
import com.likelion.backend.domain.catalog.entity.AddOnProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnProductRepository extends JpaRepository<AddOnProduct, Long> {

  List<AddOnProduct> findAllByActiveTrueOrderBySortOrderAscIdAsc();

  List<AddOnProduct> findAllByCategoryAndActiveTrueOrderBySortOrderAscIdAsc(AddOnCategory category);

  Optional<AddOnProduct> findByIdAndActiveTrue(Long id);

  long countByActiveTrue();
}
