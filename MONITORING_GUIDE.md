# Spring Boot 모니터링 시스템 가이드

## 구현 완료 항목 ✅

### 1. Spring Actuator 설정
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 2. 커스텀 메트릭
- **매출 생성 카운터**: `sales.created.total`
- **매출 금액 카운터**: `sales.amount.total`
- **정산 처리 시간**: `settlement.processing.time`
- **결제 타입별 매출**: `sales.by.payment.type.total`

### 3. Health Indicators
- **RabbitMQ**: 연결 상태, 큐 메시지 수
- **Redis**: PING 테스트, 캐시 동작 확인
- **Database**: 연결 상태, 테이블/데이터 수

## 모니터링 엔드포인트

### Actuator 기본 엔드포인트
```bash
# 헬스 체크
GET /actuator/health

# 메트릭 조회
GET /actuator/metrics

# Prometheus 메트릭
GET /actuator/prometheus
```

### 커스텀 모니터링 API
```bash
# 시스템 헬스 체크
GET /api/monitoring/health

# 주요 메트릭 요약
GET /api/monitoring/metrics/summary

# 특정 메트릭 조회
GET /api/monitoring/metrics/custom?metricName=sales.created.total

# Prometheus 포맷 메트릭
GET /api/monitoring/prometheus

# 시스템 정보
GET /api/monitoring/system/info
```

## 메트릭 종류

### 비즈니스 메트릭
| 메트릭명 | 타입 | 설명 |
|----------|------|------|
| `sales.created.total` | Counter | 총 매출 건수 |
| `sales.amount.total` | Counter | 총 매출 금액 |
| `sales.by.payment.type.total` | Counter | 결제 타입별 매출 |
| `settlement.processing.time` | Timer | 정산 처리 시간 |
| `settlement.result.total` | Counter | 정산 결과 (성공/실패) |

### 시스템 메트릭
| 메트릭명 | 타입 | 설명 |
|----------|------|------|
| `api.calls.total` | Counter | API 호출 수 |
| `batch.execution.total` | Counter | 배치 실행 수 |
| `http.server.requests` | Timer | HTTP 요청 응답시간 |

### Health Check 항목
| 컴포넌트 | 체크 항목 |
|----------|-----------|
| **customRabbitMQ** | 연결 상태, 큐 메시지 수 |
| **customRedis** | PING, 캐시 테스트, 메모리 사용량 |
| **customDatabase** | 연결 유효성, 테이블 수, 매출 데이터 수 |

## 사용 예제

### 1. 매출 생성 시 메트릭 기록
```java
// SalesService에서 자동 호출
metricsService.recordSaleCreated(amount, paymentType, channel);
```

### 2. 정산 처리 시간 측정
```java
// SettlementBatchService에서 자동 호출
Timer.Sample sample = metricsService.startSettlementTimer();
// ... 정산 로직 ...
metricsService.recordSettlementCompleted(sample, count, amount);
```

### 3. Health Check 응답 예제
```json
{
  "status": "UP",
  "components": {
    "customRabbitMQ": {
      "status": "UP",
      "details": {
        "status": "Connected",
        "sales_queue_messages": 0,
        "connection": "Active"
      }
    },
    "customRedis": {
      "status": "UP",
      "details": {
        "ping": "PONG",
        "cache_test": "PASS",
        "redis_version": "7.0.0"
      }
    }
  }
}
```

### 4. 메트릭 요약 응답 예제
```json
{
  "total_sales_count": 1250.0,
  "total_sales_amount": 15750000.0,
  "settlement_success_count": 30.0,
  "settlement_failure_count": 2.0,
  "avg_settlement_time": 2.45
}
```

## Prometheus 연동

### 메트릭 수집 설정
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'mini-today-sales'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### Grafana 대시보드 쿼리 예제
```promql
# 시간당 매출 건수
rate(sales_created_total[1h])

# 평균 정산 처리 시간
rate(settlement_processing_time_sum[5m]) / rate(settlement_processing_time_count[5m])

# 시스템 헬스 상태
up{job="mini-today-sales"}
```

## 알림 설정 (예시)

### Prometheus AlertManager 규칙
```yaml
groups:
  - name: mini-today-sales
    rules:
      - alert: HighSettlementFailureRate
        expr: rate(settlement_result_total{result="failure"}[5m]) > 0.1
        for: 2m
        annotations:
          summary: "정산 실패율 높음"

      - alert: ServiceDown
        expr: up{job="mini-today-sales"} == 0
        for: 1m
        annotations:
          summary: "서비스 다운"
```

## 모니터링 대시보드

Swagger UI에서 확인 가능한 모니터링 API:
- http://localhost:8080/swagger-ui.html
- "Monitoring" 태그 섹션에서 테스트

실제 메트릭 확인:
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/metrics
- http://localhost:8080/actuator/prometheus