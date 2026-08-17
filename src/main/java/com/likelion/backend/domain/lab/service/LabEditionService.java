package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.LabDesign;
import com.likelion.backend.domain.lab.entity.ProductionStatus;
import com.likelion.backend.domain.lab.repository.LabDesignRepository;
import com.likelion.backend.domain.store.dto.StoreStockResponseDto;
import com.likelion.backend.domain.store.entity.StoreStock;
import com.likelion.backend.domain.store.repository.StoreStockRepository;
import com.likelion.backend.global.exception.CustomException;
import com.likelion.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabEditionService {

    private final LabDesignRepository labDesignRepository;
    private final StoreStockRepository storeStockRepository;

    // 컨트롤러에서 호출할 메서드 구현
    public List<StoreStockResponseDto> getStoreStocks(Long editionId) {
        LabDesign edition = labDesignRepository.findById(editionId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.LAB_DESIGN_NOT_FOUND));
        if (edition.getProductionStatus() == ProductionStatus.VIRTUAL) {
            throw new CustomException(GlobalErrorCode.LAB_DESIGN_NOT_FOUND);
        }

        // 1. 가방 ID로 매장별 재고 리스트를 DB에서 조회
        List<StoreStock> storeStocks = storeStockRepository.findAllByLabDesignId(editionId);

        // 2. 조회한 엔티티 리스트를 프론트엔드 반환용 DTO 리스트로 변환
        return storeStocks.stream()
                .map(StoreStockResponseDto::new)
                .collect(Collectors.toList());
    }
}
