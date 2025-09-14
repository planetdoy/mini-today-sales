# 운영 환경 모니터링 실무 가이드

## 1. 운영 모니터링 전략

### 📊 모니터링 레벨별 접근

#### Level 1: 기본 헬스체크 (1분 간격)
```bash
# 자동화된 헬스체크
curl -f http://your-app:8080/actuator/health || alert "Service Down"

# 핵심 컴포넌트 상태
- Database: 연결 상태, 응답시간
- Redis: PING 응답, 메모리 사용량
- RabbitMQ: 연결 상태, 큐 길이
```

#### Level 2: 비즈니스 메트릭 (5분 간격)
```bash
# 매출 관련 알림
- 시간당 매출 0건 → 즉시 알림
- 정산 실패율 > 5% → 긴급 알림
- API 응답시간 > 3초 → 경고 알림
```

#### Level 3: 운영 최적화 (30분 간격)
```bash
# 성능 및 리소스 모니터링
- JVM 메모리 사용률 > 80%
- CPU 사용률 > 70% (5분 지속)
- 디스크 사용률 > 85%
```

## 2. 실제 운영 시나리오

### 🚨 장애 상황별 대응

#### 시나리오 1: 정산 배치 실패
```bash
# 문제 감지
- settlement.result.total{result="failure"} 증가
- settlement.processing.time 메트릭 없음

# 즉시 대응
1. /api/monitoring/health → 전체 시스템 상태 확인
2. /actuator/health → 상세 컴포넌트 상태
3. /api/settlements/date/2024-01-15 → 해당일 정산 상태
4. /api/settlements/1/reprocess → 수동 재처리
```

#### 시나리오 2: 매출 급감
```bash
# 문제 감지
- sales.created.total 증가율 급락
- API 호출은 정상인데 매출 0건

# 분석 및 대응
1. /api/monitoring/metrics/summary → 전체 지표 확인
2. Payment Gateway 연동 상태 확인
3. 특정 결제수단 장애 여부 확인
4. 로그에서 오류 패턴 분석
```

#### 시나리오 3: 응답 지연
```bash
# 문제 감지
- http.server.requests > 3초 지속
- Database 연결 지연

# 대응 절차
1. /api/monitoring/system/info → JVM 메모리 확인
2. Database 커넥션 풀 상태 확인
3. Redis 캐시 히트율 확인
4. 필요시 인스턴스 스케일 아웃
```

## 3. Prometheus + Grafana 실전 구성

### Prometheus 설정 (prometheus.yml)
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "mini-today-sales-rules.yml"

scrape_configs:
  - job_name: 'mini-today-sales'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    scrape_timeout: 10s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### 알림 규칙 (mini-today-sales-rules.yml)
```yaml
groups:
  - name: mini-today-sales-critical
    rules:
      # 서비스 다운
      - alert: ServiceDown
        expr: up{job="mini-today-sales"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Mini Today Sales 서비스 다운"
          description: "{{ $labels.instance }}에서 서비스가 응답하지 않습니다"

      # 정산 실패
      - alert: HighSettlementFailureRate
        expr: rate(settlement_result_total{result="failure"}[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "정산 실패율 높음"
          description: "정산 실패율이 5%를 초과했습니다"

      # 매출 급감
      - alert: NoSalesDetected
        expr: rate(sales_created_total[1h]) == 0
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "매출 발생 없음"
          description: "지난 1시간 동안 매출이 발생하지 않았습니다"

  - name: mini-today-sales-warning
    rules:
      # 응답 시간 지연
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API 응답시간 지연"
          description: "95% 응답시간이 3초를 초과했습니다"

      # JVM 메모리 높음
      - alert: HighJVMMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM 힙 메모리 사용률 높음"
          description: "힙 메모리 사용률이 80%를 초과했습니다"
```

### Grafana 대시보드 쿼리

#### 비즈니스 대시보드
```promql
# 실시간 매출 현황 (시간당)
rate(sales_created_total[1h]) * 3600

# 매출 금액 추이 (일별)
increase(sales_amount_total[1d])

# 결제 타입별 분포
sales_by_payment_type_total

# 정산 성공률
rate(settlement_result_total{result="success"}[5m]) /
rate(settlement_result_total[5m]) * 100
```

#### 시스템 대시보드
```promql
# API 응답시간 (95%)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# JVM 힙 메모리 사용률
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# 데이터베이스 연결 수
hikaricp_connections_active

# Redis 연결 상태
redis_connected_clients
```

## 4. 실제 운영 체크리스트

### 일일 점검 (오전 9시)
```bash
□ 전날 정산 완료 여부 확인
□ 야간 배치 작업 성공 여부
□ 시스템 리소스 사용률 확인
□ 에러 로그 패턴 분석
□ 외부 API 응답률 확인
```

### 주간 점검 (월요일)
```bash
□ 주간 매출 트렌드 분석
□ 성능 지표 변화 분석
□ 알림 임계값 적정성 검토
□ 백업 및 복구 테스트
□ 보안 패치 적용 계획
```

### 월간 점검 (매월 1일)
```bash
□ 용량 계획 및 스케일링 검토
□ 메트릭 보관 정책 검토
□ 장애 대응 프로세스 개선
□ 모니터링 도구 업데이트
□ 성능 최적화 계획 수립
```

## 5. 장애 대응 플레이북

### 🔥 긴급 상황 (P0)
```bash
# 서비스 완전 다운
1. 즉시 대체 서비스 활성화
2. 장애 원인 파악 (로그, 메트릭)
3. 긴급 패치 또는 롤백 결정
4. 고객 공지 및 보상 절차
5. 사후 분석 및 재발 방지
```

### ⚠️ 중요 상황 (P1)
```bash
# 정산 시스템 장애
1. 수동 정산으로 임시 대응
2. 데이터 정합성 확인
3. 근본 원인 분석 후 수정
4. 자동 정산 시스템 복구
5. 정산 데이터 검증
```

### 📢 경고 상황 (P2)
```bash
# 성능 지연 또는 부분 장애
1. 모니터링 지표 상세 분석
2. 임시 우회 방안 검토
3. 부하 분산 또는 캐시 최적화
4. 점진적 개선 계획 수립
5. 예방 조치 적용
```

## 6. 모니터링 도구 연동

### Slack 알림 연동
```yaml
# alertmanager.yml
route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
- name: 'web.hook'
  slack_configs:
  - api_url: 'YOUR_SLACK_WEBHOOK_URL'
    channel: '#alerts'
    title: 'Mini Today Sales Alert'
    text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

### PagerDuty 연동 (24시간 대응)
```yaml
receivers:
- name: 'pagerduty'
  pagerduty_configs:
  - service_key: 'YOUR_PAGERDUTY_KEY'
    description: '{{ .GroupLabels.alertname }}'
```

## 7. 성능 최적화 가이드

### 메트릭 수집 최적화
```yaml
# application.yml
management:
  metrics:
    distribution:
      # 히스토그램 비활성화로 메모리 절약
      percentiles-histogram:
        http.server.requests: false
    export:
      prometheus:
        # 수집 간격 조정
        step: 30s
```

### 알림 피로도 방지
```promql
# 알림 억제 규칙 (중복 방지)
- name: suppress-chatty-alerts
  source_match:
    alertname: HighResponseTime
  target_match:
    alertname: ServiceDown
  equal: ['instance']
```

이 가이드로 실제 운영 환경에서 안정적이고 효과적인 모니터링을 구축할 수 있습니다! 🎯