package com.likelion.backend.domain.lab.entity; // 패키지 경로는 상황에 맞게 수정!

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseProduct {
    TRACY_SATCHEL("Tracy Satchel"),
    PINA_TAMBOURINE_BAG("Pina Tambourine Bag"),
    AREN_VANITY_CASE("Aren Vanity Case"),
    ELLA_BOSTON_BAG("Ella Boston Bag"),
    TONI_TOP_ZIP_SHOPPER("Toni Top-Zip Shopper"),
    STARK_SIDE_STUDS_BACKPACK("Stark Side Studs Backpack");

    private final String productName;
}