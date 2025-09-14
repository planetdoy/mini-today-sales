package com.okpos.todaysales.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "모니터링 및 메트릭 API")
public class MonitoringController {

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final MeterRegistry meterRegistry;

    @GetMapping("/health")
    @Operation(summary = "시스템 헬스 체크", description = "전체 시스템의 상태를 확인합니다")
    public ResponseEntity<Object> getHealth() {
        return ResponseEntity.ok(healthEndpoint.health());
    }

    @GetMapping("/metrics/summary")
    @Operation(summary = "주요 메트릭 요약", description = "핵심 비즈니스 메트릭을 요약해서 보여줍니다")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 매출 관련 메트릭
        summary.put("total_sales_count", getMetricValue("sales.created.total"));
        summary.put("total_sales_amount", getMetricValue("sales.amount.total"));

        // 정산 관련 메트릭
        summary.put("settlement_success_count", getMetricValue("settlement.result.total", "result", "success"));
        summary.put("settlement_failure_count", getMetricValue("settlement.result.total", "result", "failure"));

        // API 호출 메트릭
        summary.put("total_api_calls", getMetricValue("api.calls.total"));

        // 응답 시간 메트릭
        summary.put("avg_settlement_time", getMetricValue("settlement.processing.time", "statistic", "mean"));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/metrics/custom")
    @Operation(summary = "커스텀 메트릭 조회", description = "특정 메트릭의 상세 정보를 조회합니다")
    public ResponseEntity<MetricsEndpoint.MetricResponse> getCustomMetric(
            @RequestParam String metricName,
            @RequestParam(required = false) String tag) {

        if (tag != null) {
            String[] tags = tag.split(",");
            return ResponseEntity.ok(metricsEndpoint.metric(metricName, java.util.Arrays.asList(tags)));
        } else {
            return ResponseEntity.ok(metricsEndpoint.metric(metricName, null));
        }
    }

    @GetMapping("/prometheus")
    @Operation(summary = "Prometheus 메트릭", description = "Prometheus 형식의 메트릭을 조회합니다")
    public ResponseEntity<String> getPrometheusMetrics() {
        if (meterRegistry instanceof PrometheusMeterRegistry) {
            PrometheusMeterRegistry prometheusMeterRegistry = (PrometheusMeterRegistry) meterRegistry;
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
                    .body(prometheusMeterRegistry.scrape());
        } else {
            return ResponseEntity.badRequest()
                    .body("Prometheus registry not available");
        }
    }

    @GetMapping("/system/info")
    @Operation(summary = "시스템 정보", description = "시스템 운영 정보를 조회합니다")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();

        // JVM 정보
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("jvm_memory_total", runtime.totalMemory());
        systemInfo.put("jvm_memory_free", runtime.freeMemory());
        systemInfo.put("jvm_memory_used", runtime.totalMemory() - runtime.freeMemory());
        systemInfo.put("jvm_memory_max", runtime.maxMemory());

        // 시스템 정보
        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("java_version", System.getProperty("java.version"));
        systemInfo.put("os_name", System.getProperty("os.name"));
        systemInfo.put("uptime", System.currentTimeMillis());

        return ResponseEntity.ok(systemInfo);
    }

    private Double getMetricValue(String metricName) {
        try {
            MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(metricName, null);
            if (metric != null && !metric.getMeasurements().isEmpty()) {
                return metric.getMeasurements().get(0).getValue();
            }
        } catch (Exception e) {
            // 메트릭이 없으면 0 반환
        }
        return 0.0;
    }

    private Double getMetricValue(String metricName, String tagKey, String tagValue) {
        try {
            java.util.List<String> tags = java.util.Arrays.asList(tagKey + ":" + tagValue);
            MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(metricName, tags);
            if (metric != null && !metric.getMeasurements().isEmpty()) {
                return metric.getMeasurements().get(0).getValue();
            }
        } catch (Exception e) {
            // 메트릭이 없으면 0 반환
        }
        return 0.0;
    }
}