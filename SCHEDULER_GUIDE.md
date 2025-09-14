# Spring Scheduler 배치 작업 가이드

## 기본 동작 조건 ✅

```java
@SpringBootApplication
@EnableScheduling  // ✅ 필수
public class Application {

@Scheduled(cron = "0 0 2 * * *")  // ✅ 기본으로 동작함
public void runDailySettlement() {
```

**답**: 네, 이 설정만으로도 **기본적으로는 동작합니다**!

## 하지만 운영 환경에서는 추가 설정 필요

### 1. 타임존 설정 ⚠️ 중요
```java
// 서버 시간이 아닌 한국 시간 기준
@Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
```

### 2. 스레드 풀 설정
```java
@Configuration
public class SchedulerConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("settlement-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
```

### 3. 중복 실행 방지
```java
@Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
public void runDailySettlement() {
    // 이미 정산된 날짜인지 체크
    if (settlementRepository.existsBySettlementDate(yesterday)) {
        log.warn("이미 정산된 날짜입니다. 스킵: {}", yesterday);
        return;
    }
    // 정산 로직...
}
```

## 운영 시 고려사항

### 1. 로그 모니터링
```yaml
logging:
  level:
    org.springframework.scheduling: INFO
    com.okpos.todaysales: DEBUG
```

### 2. 분산 환경 대응
```java
// 여러 서버에서 동시 실행 방지
@SchedulerLock(name = "dailySettlement")
@Scheduled(cron = "0 0 2 * * *")
public void runDailySettlement() {
    // ShedLock 라이브러리 사용 권장
}
```

### 3. 예외 상황 대응
```java
@Scheduled(cron = "0 0 2 * * *")
public void runDailySettlement() {
    try {
        processSettlement(yesterday);
    } catch (Exception e) {
        // 실패 알림 발송
        sendFailureNotification(yesterday, e.getMessage());
        // 재시도 로직 (선택사항)
    }
}
```

### 4. 스케줄러 비활성화 (필요시)
```yaml
settlement:
  batch:
    enabled: false  # 특정 환경에서 비활성화
```

```java
@ConditionalOnProperty(name = "settlement.batch.enabled", havingValue = "true", matchIfMissing = true)
@Scheduled(cron = "0 0 2 * * *")
public void runDailySettlement() {
    // enabled=true일 때만 실행
}
```

## 크론 표현식 참고

| 표현식 | 의미 |
|--------|------|
| `0 0 2 * * *` | 매일 새벽 2시 |
| `0 0 2 * * MON-FRI` | 평일 새벽 2시 |
| `0 0 */6 * * *` | 6시간마다 |
| `0 30 1 1 * *` | 매월 1일 새벽 1시 30분 |
| `0 0 2 L * *` | 매월 마지막 날 새벽 2시 |

## 테스트 방법

### 1. 개발 환경 테스트
```java
// 1분마다 실행으로 변경하여 테스트
@Scheduled(cron = "0 */1 * * * *")  // 임시 설정
```

### 2. 수동 실행
```bash
# Swagger UI 또는 직접 API 호출
POST /api/settlements/manual?settlementDate=2024-01-01
```

### 3. 로그 확인
```bash
# 스케줄러 실행 로그 모니터링
tail -f logs/application.log | grep "일일 정산 배치"
```

## 주의사항 ⚠️

1. **서버 재시작**: 스케줄러가 재시작되며 기존 작업은 중단
2. **긴 작업**: 다음 스케줄 시간 전에 완료되어야 함
3. **예외 처리**: 예외 발생 시 다음 스케줄까지 대기
4. **DB 커넥션**: 장시간 작업 시 커넥션 풀 고려
5. **메모리**: 대용량 데이터 처리 시 OOM 주의

## 결론

- **기본 동작**: `@EnableScheduling` + `@Scheduled`만으로도 동작
- **운영 환경**: 타임존, 중복 방지, 예외 처리, 모니터링 필수
- **확장성**: 분산 환경에서는 ShedLock 등 추가 도구 필요