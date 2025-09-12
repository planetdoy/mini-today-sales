package com.okpos.todaysales.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
    CARD("신용카드"),
    CASH("현금"),
    BANK_TRANSFER("계좌이체"),
    MOBILE_PAY("모바일페이"),
    POINT("포인트"),
    VOUCHER("상품권");
    
    private final String description;
}
