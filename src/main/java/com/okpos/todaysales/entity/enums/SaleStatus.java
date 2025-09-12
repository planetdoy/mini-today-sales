package com.okpos.todaysales.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaleStatus {
    PENDING("대기"),
    COMPLETED("완료"),
    CANCELLED("취소"),
    REFUNDED("환불");
    
    private final String description;
}
