package com.okpos.todaysales.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final Counter saleCreatedCounter;
    private final Counter saleAmountCounter;
    private final Counter saleByPaymentTypeCounter;
    private final Timer settlementProcessingTimer;
    private final Counter settlementResultCounter;
    private final Counter settlementFailureCounter;
    private final Counter apiCallCounter;
    private final io.micrometer.core.instrument.MeterRegistry meterRegistry;

    /**
     * 매출 생성 메트릭 기록
     */
    public void recordSaleCreated(BigDecimal amount, String paymentType, String channel) {
        // 매출 건수 증가
        saleCreatedCounter.increment();

        // 매출 금액 증가
        saleAmountCounter.increment(amount.doubleValue());

        // 결제 타입별 매출 증가
        Counter.builder("sales.by.payment.type.total")
                .tag("payment_type", paymentType)
                .tag("channel", channel)
                .register(meterRegistry)
                .increment();

        log.debug("매출 메트릭 기록: 금액={}, 결제타입={}, 채널={}", amount, paymentType, channel);
    }

    /**
     * 정산 처리 시간 메트릭 기록
     */
    public Timer.Sample startSettlementTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 정산 완료 메트릭 기록
     */
    public void recordSettlementCompleted(Timer.Sample sample, int transactionCount, BigDecimal totalAmount) {
        // 처리 시간 기록
        sample.stop(settlementProcessingTimer);

        // 정산 성공 카운터
        Counter.builder("settlement.result.total")
                .tag("result", "success")
                .tag("transaction_count", String.valueOf(transactionCount))
                .register(meterRegistry)
                .increment();

        log.info("정산 완료 메트릭 기록: 거래수={}, 총액={}", transactionCount, totalAmount);
    }

    /**
     * 정산 실패 메트릭 기록
     */
    public void recordSettlementFailed(Timer.Sample sample, String errorType, String errorMessage) {
        // 처리 시간 기록 (실패한 경우에도)
        if (sample != null) {
            sample.stop(settlementProcessingTimer);
        }

        // 정산 실패 카운터
        Counter.builder("settlement.result.total")
                .tag("result", "failure")
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();

        log.warn("정산 실패 메트릭 기록: 오류타입={}, 메시지={}", errorType, errorMessage);
    }

    /**
     * API 호출 메트릭 기록
     */
    public void recordApiCall(String endpoint, String method, String status) {
        Counter.builder("api.calls.total")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 배치 작업 메트릭 기록
     */
    public void recordBatchExecution(String batchName, String status, long executionTimeMs) {
        Counter.builder("batch.execution.total")
                .tag("batch_name", batchName)
                .tag("status", status)
                .register(meterRegistry)
                .increment();

        Timer.builder("batch.execution.time")
                .tag("batch_name", batchName)
                .register(meterRegistry)
                .record(executionTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * 비즈니스 메트릭 기록 (일일 요약)
     */
    public void recordDailySummary(int totalSales, BigDecimal totalAmount, BigDecimal totalFee) {
        Counter.builder("daily.summary.sales")
                .tag("type", "total_count")
                .register(meterRegistry)
                .increment(totalSales);

        Counter.builder("daily.summary.amount")
                .tag("type", "total_amount")
                .register(meterRegistry)
                .increment(totalAmount.doubleValue());

        Counter.builder("daily.summary.fee")
                .tag("type", "total_fee")
                .register(meterRegistry)
                .increment(totalFee.doubleValue());
    }
}