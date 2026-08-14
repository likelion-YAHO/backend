package com.likelion.backend.domain.product.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AiConditionConverter implements AttributeConverter<AiCondition, String> {

  @Override
  public String convertToDatabaseColumn(AiCondition attribute) {
    return attribute == null ? null : attribute.getLabel();
  }

  @Override
  public AiCondition convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AiCondition.fromLabel(dbData);
  }
}
