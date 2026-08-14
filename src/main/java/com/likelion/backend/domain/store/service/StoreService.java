package com.likelion.backend.domain.store.service;

import com.likelion.backend.domain.store.dto.StoreResponseDto;
import com.likelion.backend.domain.store.entity.Store;
import com.likelion.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public List<StoreResponseDto> getStoresSortedByDistance(Double userLat, Double userLon) {
        // 1. DB에서 모든 매장 조회
        List<Store> stores = storeRepository.findAll();

        // 2. 각 매장별로 거리 계산 후 DTO로 변환
        return stores.stream()
                .map(store -> {
                    double distance = calculateDistance(userLat, userLon, store.getLatitude(), store.getLongitude());
                    return StoreResponseDto.builder()
                            .id(store.getId())
                            .name(store.getName())
                            .address(store.getAddress())
                            .phone(store.getPhone())
                            .latitude(store.getLatitude())
                            .longitude(store.getLongitude())
                            .distance(Math.round(distance * 100.0) / 100.0) // 소수점 둘째 자리까지 반올림
                            .build();
                })
                .sorted(Comparator.comparingDouble(StoreResponseDto::getDistance)) // 3. 가까운 거리순 정렬
                .collect(Collectors.toList());
    }

    // Haversine 공식을 이용한 두 지점 간의 직선 거리 계산 (단위: km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}