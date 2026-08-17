package com.likelion.backend.domain.store.dto;

import com.likelion.backend.domain.store.entity.StoreStock;
import lombok.Getter;

@Getter
public class StoreStockResponseDto {
    private String storeName;
    private String address;
    private String phoneNumber;
    private Integer stockCount;
    private Boolean isSoldOut;

    public StoreStockResponseDto(StoreStock storeStock) {
        this.storeName = storeStock.getStore().getName();
        this.address = storeStock.getStore().getAddress();
        this.phoneNumber = storeStock.getStore().getPhone();
        this.stockCount = storeStock.getStockCount();
        this.isSoldOut = (storeStock.getStockCount() <= 0);
    }
}