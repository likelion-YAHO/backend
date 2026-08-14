package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.dto.BaseProductResponseDto;
import com.likelion.backend.domain.lab.entity.BaseProduct;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabProductService {

    public List<BaseProductResponseDto> getBaseProducts() {
        // BaseProduct Enum의 모든 값을 DTO로 변환하여 리스트로 반환
        return Arrays.stream(BaseProduct.values())
                .map(BaseProductResponseDto::new)
                .collect(Collectors.toList());
    }
}