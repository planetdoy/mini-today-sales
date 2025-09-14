package com.okpos.todaysales.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /**
     * 매출 생성 카운터 메트릭
     */
    @Bean
    public Counter saleCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("sales.created.total")
                .description("Total number of sales created")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }

    /**
     * 매출 생성 금액 카운터
     */
    @Bean
    public Counter saleAmountCounter(MeterRegistry meterRegistry) {
        return Counter.builder("sales.amount.total")
                .description("Total amount of sales")
                .tag("application", "mini-today-sales")
                .baseUnit("KRW")
                .register(meterRegistry);
    }

    /**
     * 결제 타입별 매출 카운터
     */
    @Bean
    public Counter saleByPaymentTypeCounter(MeterRegistry meterRegistry) {
        return Counter.builder("sales.by.payment.type.total")
                .description("Total sales count by payment type")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }

    /**
     * 정산 처리 시간 타이머
     */
    @Bean
    public Timer settlementProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("settlement.processing.time")
                .description("Settlement processing time")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }

    /**
     * 정산 결과 카운터
     */
    @Bean
    public Counter settlementResultCounter(MeterRegistry meterRegistry) {
        return Counter.builder("settlement.result.total")
                .description("Settlement result counter")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }

    /**
     * 정산 실패 카운터
     */
    @Bean
    public Counter settlementFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("settlement.failure.total")
                .description("Settlement failure counter")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }

    /**
     * API 호출 카운터
     */
    @Bean
    public Counter apiCallCounter(MeterRegistry meterRegistry) {
        return Counter.builder("api.calls.total")
                .description("Total API calls")
                .tag("application", "mini-today-sales")
                .register(meterRegistry);
    }
}