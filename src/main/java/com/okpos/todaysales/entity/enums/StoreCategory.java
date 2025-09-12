package com.okpos.todaysales.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreCategory {
    RESTAURANT("식당"),
    CAFE("카페"),
    RETAIL("재판매"),
    CONVENIENCE_STORE("편의점"),
    CLOTHING("의류"),
    BEAUTY("미용"),
    HEALTH("헬스케어"),
    EDUCATION("교육"),
    ENTERTAINMENT("엔터테이먼트"),
    OTHER("기타");
    
    private final String description;
}
