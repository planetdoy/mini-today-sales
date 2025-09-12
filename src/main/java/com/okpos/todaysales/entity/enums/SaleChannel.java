package com.okpos.todaysales.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaleChannel {
    OFFLINE("오프라인"),
    ONLINE("온라인"),
    MOBILE_APP("모바일앱"),
    DELIVERY("배달"),
    TAKEOUT("테이크아웃");
    
    private final String description;
}
