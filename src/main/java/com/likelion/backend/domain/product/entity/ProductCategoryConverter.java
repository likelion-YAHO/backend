package com.likelion.backend.domain.product.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ProductCategory 타입을 쓰는 모든 필드(Product.category, Review.category)에 자동 적용된다.
 */
@Converter(autoApply = true)
public class ProductCategoryConverter implements AttributeConverter<ProductCategory, String> {

  @Override
  public String convertToDatabaseColumn(ProductCategory attribute) {
    return attribute == null ? null : attribute.getLabel();
  }

  @Override
  public ProductCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ProductCategory.fromLabel(dbData);
  }
}
