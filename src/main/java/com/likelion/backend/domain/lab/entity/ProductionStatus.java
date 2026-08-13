package com.likelion.backend.domain.lab.entity;

public enum ProductionStatus {
    VIRTUAL,        // 가상 디자인 상태 (기본값)
    SELECTED,       // MCM 본사 선정 완료
    IN_PRODUCTION,  // 실제 제품 제작 중
    READY           // 제작 완료 및 판매 대기
}