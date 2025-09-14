# 🚀 모니터링 시스템 빠른 시작 가이드

## 1. 즉시 시작하기 (5분)

### 현재 상태 확인
```bash
# 애플리케이션 실행 후 즉시 확인 가능
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/monitoring/health
curl http://localhost:8080/api/monitoring/metrics/summary
```

### Swagger UI에서 테스트
```
http://localhost:8080/swagger-ui.html
→ "Monitoring" 섹션에서 API 테스트
```

## 2. 전체 모니터링 스택 실행 (10분)

### Docker Compose로 한번에 실행
```bash
# 모니터링 스택 전체 실행
docker-compose -f docker-compose-monitoring.yml up -d

# 확인
docker-compose -f docker-compose-monitoring.yml ps
```

### 접속 URL
```bash
# 애플리케이션
http://localhost:8080

# Prometheus (메트릭 수집)
http://localhost:9090

# Grafana (대시보드)
http://localhost:3000
# ID: admin, PW: admin123

# AlertManager (알림 관리)
http://localhost:9093
```

## 3. 실전 시나리오 테스트

### 💡 시나리오 1: 정상 운영 확인
```bash
# 1. 매출 생성
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d '{
    "businessNumber": "123-45-67890",
    "amount": 50000,
    "paymentType": "CARD",
    "channel": "ONLINE",
    "orderNumber": "ORDER-001",
    "transactionTime": "2024-01-15T14:30:00"
  }'

# 2. 메트릭 확인
curl http://localhost:8080/api/monitoring/metrics/summary

# 3. Grafana에서 실시간 차트 확인
# → sales_created_total 증가 확인
```

### 🚨 시나리오 2: 장애 상황 시뮬레이션
```bash
# 1. 데이터베이스 연결 끊기
docker-compose -f docker-compose-monitoring.yml stop mysql

# 2. 헬스체크 확인 (DOWN 상태)
curl http://localhost:8080/actuator/health

# 3. Prometheus Alert 확인
# → http://localhost:9090/alerts

# 4. 복구
docker-compose -f docker-compose-monitoring.yml start mysql
```

### 📊 시나리오 3: 정산 배치 모니터링
```bash
# 1. 수동 정산 실행
curl -X POST "http://localhost:8080/api/settlements/manual?settlementDate=2024-01-15"

# 2. 정산 시간 메트릭 확인
curl "http://localhost:8080/api/monitoring/metrics/custom?metricName=settlement.processing.time"

# 3. Grafana에서 정산 대시보드 확인
```

## 4. 일일 운영 체크리스트

### 🌅 오전 체크 (9:00 AM)
```bash
□ 시스템 상태: curl http://localhost:8080/actuator/health
□ 야간 정산: curl http://localhost:8080/api/settlements/date/$(date -d yesterday +%Y-%m-%d)
□ 메트릭 요약: curl http://localhost:8080/api/monitoring/metrics/summary
□ 알림 확인: http://localhost:9093 (AlertManager)
```

### 🌆 저녁 체크 (6:00 PM)
```bash
□ 당일 매출: 매출 메트릭 확인
□ 시스템 리소스: JVM 메모리, CPU 사용률
□ 에러 로그: 애플리케이션 로그 점검
□ 내일 배치: 정산 준비 상태 확인
```

## 5. 주요 메트릭 해석 가이드

### 비즈니스 메트릭
```bash
# 시간당 매출 건수 (정상: 10-100건/시간)
sales_created_total

# 평균 주문 금액 (정상: 30,000-100,000원)
sales_amount_total / sales_created_total

# 정산 성공률 (목표: 99.9% 이상)
settlement_result_total{result="success"} / settlement_result_total
```

### 시스템 메트릭
```bash
# API 응답시간 (목표: 95% < 1초)
http_server_requests_seconds{quantile="0.95"}

# JVM 힙 메모리 (경고: > 80%)
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# 데이터베이스 연결 수 (정상: < 20개)
hikaricp_connections_active
```

## 6. 장애 대응 순서

### 🔴 Critical (즉시 대응)
```bash
1. 서비스 다운
   → 로드밸런서에서 제외
   → 원인 파악 (로그, 메트릭)
   → 긴급 패치 또는 롤백

2. 정산 실패
   → 수동 정산으로 우회
   → 데이터 정합성 확인
   → 근본 원인 수정
```

### 🟡 Warning (30분 내 대응)
```bash
1. 응답 지연
   → 트래픽 패턴 분석
   → 데이터베이스 쿼리 최적화
   → 캐시 설정 조정

2. 메모리 부족
   → JVM 힙 덤프 분석
   → 메모리 리크 확인
   → 인스턴스 스케일링
```

## 7. 커스터마이징 가이드

### 새로운 메트릭 추가
```java
// 1. MetricsConfig에 메트릭 정의
@Bean
public Counter customBusinessCounter(MeterRegistry registry) {
    return Counter.builder("custom.business.metric")
        .description("Custom business metric")
        .register(registry);
}

// 2. 서비스에서 사용
customBusinessCounter.increment();

// 3. Prometheus에서 확인
custom_business_metric_total
```

### 새로운 알림 규칙 추가
```yaml
# monitoring/prometheus/rules/custom.yml
- alert: CustomBusinessAlert
  expr: custom_business_metric_total > 1000
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Custom business metric exceeded threshold"
```

### Grafana 대시보드 추가
```bash
# 1. Grafana UI에서 대시보드 생성
# 2. JSON으로 내보내기
# 3. monitoring/grafana/dashboards/ 폴더에 저장
# 4. 재시작 시 자동 로드
```

## 8. 성능 최적화 팁

### 메트릭 수집 최적화
```yaml
# application.yml
management:
  metrics:
    export:
      prometheus:
        step: 30s  # 수집 간격 조정
    distribution:
      percentiles-histogram:
        http.server.requests: false  # 불필요한 히스토그램 비활성화
```

### 알림 최적화
```yaml
# alertmanager.yml
route:
  group_wait: 30s      # 그룹 대기 시간
  group_interval: 5m   # 그룹 간격
  repeat_interval: 1h  # 반복 간격
```

## 9. 문제 해결 FAQ

### Q: 메트릭이 수집되지 않아요
```bash
# 1. 엔드포인트 확인
curl http://localhost:8080/actuator/prometheus

# 2. Prometheus 타겟 상태 확인
http://localhost:9090/targets

# 3. 네트워크 연결 확인
docker network ls
```

### Q: 알림이 오지 않아요
```bash
# 1. AlertManager 설정 확인
http://localhost:9093

# 2. 알림 규칙 문법 확인
http://localhost:9090/rules

# 3. 라우팅 설정 확인
# alertmanager.yml의 route 섹션
```

### Q: Grafana 대시보드가 비어있어요
```bash
# 1. 데이터소스 연결 확인
# Grafana → Configuration → Data Sources

# 2. 쿼리 문법 확인
# Prometheus 쿼리 브라우저에서 테스트

# 3. 시간 범위 확인
# 대시보드 상단의 시간 선택기
```

## 10. 다음 단계

### 고급 기능 추가
```bash
□ Jaeger 분산 추적 연동
□ ELK 스택 로그 수집
□ Custom Exporter 개발
□ 자동 스케일링 연동
□ 카나리 배포 모니터링
```

이 가이드로 5분 만에 모니터링을 시작하고, 점진적으로 고도화할 수 있습니다! 🎯