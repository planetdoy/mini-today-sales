# 🧪 통합 테스트 가이드

## 개요

Mini Today Sales 프로젝트의 통합 테스트는 **TestContainers**를 사용하여 실제 운영 환경과 유사한 조건에서 테스트를 실행합니다.

## 테스트 구성

### 📦 TestContainers 구성

- **MySQL 8.0**: 실제 데이터베이스 환경
- **Redis 7-alpine**: 캐시 및 세션 저장소
- **RabbitMQ 3-management**: 메시지 큐 시스템

### 🎯 테스트 범위

1. **매출 시스템 통합 테스트**
   - 매출 생성 및 조회
   - 결제 타입별 처리
   - 메트릭 업데이트 확인

2. **모니터링 시스템 테스트**
   - Health Check (Actuator + 커스텀)
   - Prometheus 메트릭 수집
   - 시스템 정보 조회

3. **데이터 유효성 검증**
   - Request DTO 유효성 검사
   - 비즈니스 로직 검증

## 🚀 테스트 실행 방법

### 1. 전체 통합 테스트 실행

```bash
# 모든 통합 테스트 실행
mvn test -Dtest="*IntegrationTest"

# 특정 통합 테스트 클래스 실행
mvn test -Dtest=SimpleIntegrationTest
```

### 2. 개별 테스트 메서드 실행

```bash
# 특정 테스트 메서드만 실행
mvn test -Dtest=SimpleIntegrationTest#createAndRetrieveSalesTest

# 헬스체크 테스트만 실행
mvn test -Dtest=SimpleIntegrationTest#healthCheckTest
```

### 3. 테스트 프로파일 설정

```bash
# 테스트 전용 프로파일로 실행
mvn test -Dspring.profiles.active=test
```

## 📋 테스트 시나리오

### 1. 매출 생성 및 조회 테스트

```java
@Test
@DisplayName("매출 생성 및 조회 테스트")
void createAndRetrieveSalesTest() {
    // Given: 매출 데이터 준비
    // When: API 호출로 매출 생성
    // Then: 생성된 매출 조회 및 검증
}
```

**검증 항목:**
- ✅ 매출 데이터 정상 생성
- ✅ JSON 응답 형식 확인
- ✅ 데이터베이스 저장 검증
- ✅ 페이징 처리 확인

### 2. 헬스체크 통합 테스트

```java
@Test
@DisplayName("헬스체크 통합 테스트")
void healthCheckTest() {
    // Spring Actuator + 커스텀 헬스체크 검증
}
```

**검증 항목:**
- ✅ Spring Actuator `/actuator/health`
- ✅ 커스텀 헬스체크 `/api/monitoring/health`
- ✅ 각 컴포넌트 상태 (DB, Redis, RabbitMQ)

### 3. 메트릭 시스템 테스트

```java
@Test
@DisplayName("매출 생성 후 메트릭 업데이트 확인")
void salesMetricsUpdateTest() {
    // 매출 생성 → 메트릭 자동 업데이트 검증
}
```

**검증 항목:**
- ✅ Micrometer 메트릭 수집
- ✅ Prometheus 엔드포인트 `/actuator/prometheus`
- ✅ 커스텀 메트릭 업데이트
- ✅ 비동기 메트릭 처리

## 🔧 테스트 설정

### TestContainers 설정

```java
@Container
protected static final MySQLContainer<?> mysqlContainer =
    new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("today_sales_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);
```

### 동적 프로퍼티 주입

```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.redis.host", redisContainer::getHost);
    registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
}
```

## 📊 테스트 결과 분석

### 1. 성공 시나리오

```bash
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 2. 주요 검증 포인트

- **데이터 정합성**: 생성된 데이터가 정확히 저장되고 조회되는지
- **API 응답**: HTTP 상태 코드와 JSON 구조가 올바른지
- **메트릭 수집**: 비즈니스 이벤트가 메트릭으로 정상 기록되는지
- **헬스체크**: 모든 외부 의존성이 정상 동작하는지

## 🐳 Docker 환경 요구사항

### 시스템 요구사항

- **Docker Desktop**: 최신 버전 권장
- **메모리**: 최소 4GB 이상
- **디스크**: 여유 공간 2GB 이상

### 컨테이너 포트

- MySQL: 동적 할당
- Redis: 동적 할당
- RabbitMQ: 동적 할당 (5672, 15672)

## 🛠️ 문제 해결

### 1. TestContainers 시작 실패

```bash
# Docker 상태 확인
docker info

# Docker Desktop 재시작
# Windows: Docker Desktop 재시작
# macOS: Docker Desktop 재시작
```

### 2. 메모리 부족 오류

```bash
# Docker 메모리 설정 증가
# Docker Desktop → Settings → Resources → Memory: 4GB+
```

### 3. 포트 충돌

```bash
# 사용 중인 포트 확인
netstat -ano | findstr :8080
netstat -ano | findstr :3306

# 필요시 관련 프로세스 종료
```

### 4. 테스트 타임아웃

```yaml
# application-test.yml
test:
  async:
    timeout: 30000  # 30초로 증가
```

## 🎯 모범 사례

### 1. 테스트 격리

```java
@BeforeEach
@Transactional
void setUp() {
    // 각 테스트 전 데이터 정리
    settlementRepository.deleteAll();
    saleRepository.deleteAll();
    storeRepository.deleteAll();
}
```

### 2. 비동기 검증

```java
// Awaitility 사용
await().untilAsserted(() -> {
    mockMvc.perform(get("/api/monitoring/metrics/summary"))
            .andExpect(jsonPath("$.total_sales_count").value(greaterThan(0.0)));
});
```

### 3. 테스트 데이터 관리

```java
// Builder 패턴으로 테스트 데이터 생성
SaleRequest saleRequest = SaleRequest.builder()
    .businessNumber("123-45-67890")
    .amount(new BigDecimal("50000"))
    .paymentType(PaymentType.CARD)
    .build();
```

## 📈 성능 최적화

### 1. 컨테이너 재사용

```java
@Container
@Reusable
protected static final MySQLContainer<?> mysqlContainer = ...
```

### 2. 테스트 병렬 실행

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

이 가이드를 따라 안정적이고 신뢰할 수 있는 통합 테스트를 실행할 수 있습니다! 🎉