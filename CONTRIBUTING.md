# 🤝 기여하기 (Contributing)

Mini Today Sales 프로젝트에 기여해주셔서 감사합니다! 이 문서는 프로젝트에 효과적으로 기여하기 위한 가이드라인을 제공합니다.

## 📋 목차

- [시작하기 전에](#-시작하기-전에)
- [개발 환경 설정](#-개발-환경-설정)
- [코딩 컨벤션](#-코딩-컨벤션)
- [브랜치 전략](#-브랜치-전략)
- [커밋 가이드라인](#-커밋-가이드라인)
- [Pull Request 가이드](#-pull-request-가이드)
- [테스트 가이드라인](#-테스트-가이드라인)
- [문서화 가이드라인](#-문서화-가이드라인)
- [코드 리뷰 가이드라인](#-코드-리뷰-가이드라인)

## 🚀 시작하기 전에

### 기여 방법

1. **이슈 확인**: [GitHub Issues](https://github.com/your-org/mini-today-sales/issues)에서 작업할 이슈를 선택하세요
2. **토론 참여**: 기능 제안이나 아이디어는 [GitHub Discussions](https://github.com/your-org/mini-today-sales/discussions)에서 논의하세요
3. **버그 리포트**: 발견한 버그는 이슈 템플릿을 사용하여 상세히 신고해주세요

### 기여할 수 있는 영역

- 🐛 **버그 수정**: 코드의 버그나 문제점 해결
- ✨ **새 기능**: 매출 관리 관련 새로운 기능 개발
- 📚 **문서화**: API 문서, 사용자 가이드, 코드 주석 개선
- 🧪 **테스트**: 테스트 커버리지 개선 및 테스트 케이스 추가
- 🎨 **UI/UX**: 대시보드 및 모니터링 화면 개선
- ⚡ **성능 최적화**: 시스템 성능 개선

## 🛠️ 개발 환경 설정

### 시스템 요구사항

```bash
# 필수 요구사항
Java 11+
Maven 3.8+
Git 2.30+

# 권장 도구
Docker 20.10+
Docker Compose 2.0+
IntelliJ IDEA 또는 VS Code
```

### 로컬 환경 설정

1. **저장소 포크 및 클론**

```bash
# GitHub에서 저장소를 포크한 후
git clone https://github.com/your-username/mini-today-sales.git
cd mini-today-sales

# 원본 저장소를 upstream으로 추가
git remote add upstream https://github.com/original-org/mini-today-sales.git
```

2. **의존성 서비스 실행**

```bash
# Docker Compose로 MySQL, Redis, RabbitMQ 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

3. **애플리케이션 빌드 및 실행**

```bash
# 의존성 설치 및 컴파일
mvn clean compile

# 테스트 실행 (필수)
mvn test

# 애플리케이션 실행
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **개발 환경 확인**

```bash
# Health Check
curl http://localhost:8080/actuator/health

# API 문서
open http://localhost:8080/swagger-ui/index.html
```

### IDE 설정

#### IntelliJ IDEA

1. **코드 스타일 설정**
   - Settings → Editor → Code Style → Java
   - Import scheme: `ide-settings/intellij-code-style.xml`

2. **플러그인 설치**
   - Lombok
   - SonarLint
   - CheckStyle-IDEA

3. **실행 구성**
   - Spring Boot Run Configuration 생성
   - VM options: `-Dspring.profiles.active=dev`

#### VS Code

1. **필수 확장 프로그램**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **설정 파일** (.vscode/settings.json)
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.live-information.automatic-tracking.on": true
}
```

## 📝 코딩 컨벤션

### Java 코딩 스타일

#### 1. 네이밍 규칙

```java
// ✅ 올바른 예시
public class SalesController {
    private final SalesService salesService;

    @PostMapping("/webhook")
    public ResponseEntity<ServerApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody SaleRequest request) {
        // 구현
    }
}

// ❌ 잘못된 예시
public class salescontroller {
    private final SalesService ss;

    @PostMapping("/webhook")
    public ResponseEntity<ServerApiResponse<SaleResponse>> create(
            @Valid @RequestBody SaleRequest req) {
        // 구현
    }
}
```

#### 2. 패키지 구조

```
com.okpos.todaysales
├── controller/          # REST 컨트롤러
├── service/            # 비즈니스 로직
│   ├── impl/          # 서비스 구현체
│   └── external/      # 외부 서비스 연동
├── repository/         # 데이터 접근 계층
├── entity/            # JPA 엔티티
│   └── enums/        # 열거형
├── dto/               # 데이터 전송 객체
│   ├── request/      # 요청 DTO
│   └── response/     # 응답 DTO
├── config/            # 설정 클래스
├── listener/          # 이벤트 리스너
├── exception/         # 예외 클래스
└── util/              # 유틸리티 클래스
```

#### 3. 어노테이션 사용

```java
// ✅ 올바른 순서와 사용법
@Slf4j
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Validated
@Tag(name = "Sales", description = "매출 관리 API")
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "매출 등록", description = "POS에서 매출 데이터를 등록합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/webhook")
    public ResponseEntity<ServerApiResponse<SaleResponse>> createSale(
            @Parameter(description = "매출 데이터") @Valid @RequestBody SaleRequest request) {
        // 구현
    }
}
```

#### 4. 예외 처리

```java
// ✅ 비즈니스 예외 처리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        try {
            // 비즈니스 로직
            return saleResponse;
        } catch (IllegalArgumentException e) {
            log.error("잘못된 매출 데이터: {}", e.getMessage());
            throw new InvalidSaleDataException("매출 데이터가 유효하지 않습니다", e);
        } catch (Exception e) {
            log.error("매출 등록 중 오류 발생", e);
            throw new SaleProcessingException("매출 등록에 실패했습니다", e);
        }
    }
}
```

### 데이터베이스 규칙

#### 1. 엔티티 설계

```java
@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"store"})
@EqualsAndHashCode(of = "id")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### 2. Repository 규칙

```java
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // 메서드 이름으로 쿼리 생성 (간단한 경우)
    Optional<Sale> findByOrderNumber(String orderNumber);
    List<Sale> findByPaymentType(PaymentType paymentType);

    // @Query 사용 (복잡한 경우)
    @Query("SELECT s FROM Sale s WHERE s.store.id = :storeId " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "ORDER BY s.transactionTime DESC")
    List<Sale> findByStoreIdAndDateRange(@Param("storeId") Long storeId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
```

### API 설계 규칙

#### 1. REST API 규칙

```java
// ✅ RESTful API 설계
@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    // POST /api/v1/sales/webhook - 리소스 생성
    @PostMapping("/webhook")
    public ResponseEntity<ServerApiResponse<SaleResponse>> createSale(@RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/sales/{businessNumber} - 리소스 조회 (목록)
    @GetMapping("/{businessNumber}")
    public ResponseEntity<ServerApiResponse<Page<SaleResponse>>> getSales(
            @PathVariable String businessNumber,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/sales/dashboard/{businessNumber} - 특정 뷰
    @GetMapping("/dashboard/{businessNumber}")
    public ResponseEntity<ServerApiResponse<DashboardResponse>> getDashboard(
            @PathVariable String businessNumber,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate date) {
        return ResponseEntity.ok(response);
    }
}
```

#### 2. 응답 형식 표준화

```java
// ✅ 표준 응답 형식
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Long timestamp;

    public static <T> ServerApiResponse<T> success(T data) {
        return ServerApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ServerApiResponse<T> error(String message) {
        return ServerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
```

## 🌿 브랜치 전략

### Git Flow 기반 브랜치 모델

```
main                    # 운영 배포 브랜치
├── develop            # 개발 통합 브랜치
│   ├── feature/매출통계-차트-추가
│   ├── feature/결제수단-추가
│   └── feature/대시보드-개선
├── release/v1.2.0     # 릴리스 준비 브랜치
└── hotfix/긴급-버그수정   # 긴급 수정 브랜치
```

### 브랜치 명명 규칙

```bash
# 기능 개발
feature/기능명-간단설명
feature/payment-integration
feature/dashboard-chart-improvement

# 버그 수정
fix/버그명-간단설명
fix/sales-calculation-error
fix/memory-leak-issue

# 문서 업데이트
docs/문서명-설명
docs/api-documentation-update
docs/contributing-guide-improvement

# 성능 개선
perf/개선영역-설명
perf/database-query-optimization
perf/redis-cache-improvement
```

### 브랜치 작업 흐름

```bash
# 1. 최신 코드 동기화
git checkout develop
git pull upstream develop

# 2. 새 기능 브랜치 생성
git checkout -b feature/payment-integration

# 3. 작업 및 커밋
git add .
git commit -m "feat: PG사 결제 연동 API 추가"

# 4. 원격 브랜치에 푸시
git push origin feature/payment-integration

# 5. Pull Request 생성
# GitHub에서 PR 생성
```

## 💬 커밋 가이드라인

### Conventional Commits 규칙

```bash
<타입>[선택적 스코프]: <설명>

[선택적 본문]

[선택적 푸터]
```

### 커밋 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat(sales): 매출 통계 API 추가` |
| `fix` | 버그 수정 | `fix(dashboard): 차트 데이터 로딩 오류 수정` |
| `docs` | 문서 변경 | `docs: API 문서 업데이트` |
| `style` | 코드 포맷팅, 세미콜론 누락 등 | `style: 코드 포맷팅 적용` |
| `refactor` | 코드 리팩토링 | `refactor(service): 매출 계산 로직 개선` |
| `test` | 테스트 코드 추가/수정 | `test(integration): 매출 생성 테스트 추가` |
| `chore` | 빌드, 패키지 관리 등 | `chore: 의존성 업데이트` |
| `perf` | 성능 개선 | `perf(cache): Redis 캐시 최적화` |

### 커밋 메시지 예시

```bash
# ✅ 올바른 커밋 메시지
feat(sales): POS 시스템 매출 데이터 수신 API 구현

- Webhook 엔드포인트 추가 (/api/v1/sales/webhook)
- 매출 데이터 유효성 검증 로직 구현
- RabbitMQ 메시지 발행 연동

Closes #123

# ✅ 간단한 커밋
fix(dashboard): 차트 렌더링 오류 수정

# ❌ 잘못된 커밋 메시지
update code
fix bug
add feature
```

## 🔄 Pull Request 가이드

### PR 생성 전 체크리스트

- [ ] 코드가 컴파일되고 테스트가 통과하는가?
- [ ] 새로운 기능에 대한 테스트를 작성했는가?
- [ ] 코딩 컨벤션을 준수했는가?
- [ ] API 변경 시 문서를 업데이트했는가?
- [ ] 커밋 메시지가 규칙을 따르는가?

### PR 템플릿

```markdown
## 🎯 변경 사항 요약
- 매출 통계 차트 기능 추가
- 결제 수단별 분석 API 구현

## 🔧 변경 내용
### 추가된 기능
- [ ] 매출 통계 차트 컴포넌트
- [ ] 결제 수단별 분석 API
- [ ] 실시간 데이터 업데이트

### 수정된 기능
- [ ] 대시보드 레이아웃 개선
- [ ] API 응답 형식 표준화

## 🧪 테스트
- [ ] 단위 테스트 추가/수정
- [ ] 통합 테스트 추가/수정
- [ ] 수동 테스트 완료

## 📸 스크린샷 (UI 변경 시)
Before: [이전 이미지]
After: [변경 후 이미지]

## 🔗 관련 이슈
Closes #123
Related to #456

## 📝 추가 정보
- API 문서 업데이트 필요
- 성능 테스트 결과: 평균 응답시간 50ms 개선
```

### 코드 리뷰 요청 사항

1. **명확한 제목**: 변경 사항을 한 줄로 요약
2. **상세한 설명**: 왜 이 변경이 필요한지 설명
3. **테스트 증빙**: 테스트 결과 및 커버리지 정보
4. **스크린샷**: UI 변경이 있는 경우 Before/After 이미지

## 🧪 테스트 가이드라인

### 테스트 전략

#### 1. 단위 테스트 (Unit Test)

```java
@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SalesService salesService;

    @Test
    @DisplayName("매출 생성 - 성공")
    void createSale_Success() {
        // Given
        SaleRequest request = SaleRequest.builder()
                .businessNumber("123-45-67890")
                .amount(new BigDecimal("50000"))
                .paymentType(PaymentType.CARD)
                .build();

        Store store = Store.builder()
                .businessNumber("123-45-67890")
                .storeName("테스트 매장")
                .build();

        when(storeRepository.findByBusinessNumber("123-45-67890"))
                .thenReturn(Optional.of(store));
        when(saleRepository.save(any(Sale.class)))
                .thenReturn(savedSale);

        // When
        SaleResponse response = salesService.createSale(request);

        // Then
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("50000"));
        assertThat(response.getPaymentType()).isEqualTo(PaymentType.CARD);

        verify(saleRepository).save(any(Sale.class));
    }
}
```

#### 2. 통합 테스트 (Integration Test)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SalesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("매출 생성 API 통합 테스트")
    @Transactional
    void createSale_IntegrationTest() throws Exception {
        // Given
        SaleRequest request = SaleRequest.builder()
                .businessNumber("123-45-67890")
                .amount(new BigDecimal("50000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-001")
                .transactionTime(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/sales/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(50000))
                .andExpect(jsonPath("$.data.paymentType").value("CARD"));
    }
}
```

#### 3. 테스트 커버리지

```bash
# JaCoCo 테스트 커버리지 실행
mvn clean test jacoco:report

# 커버리지 보고서 확인
open target/site/jacoco/index.html
```

**목표 커버리지:**
- **전체**: 80% 이상
- **서비스 계층**: 90% 이상
- **컨트롤러 계층**: 85% 이상

### 테스트 데이터 관리

```java
// ✅ Builder 패턴으로 테스트 데이터 생성
public class TestDataBuilder {

    public static SaleRequest.SaleRequestBuilder saleRequest() {
        return SaleRequest.builder()
                .businessNumber("123-45-67890")
                .amount(new BigDecimal("50000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-" + System.currentTimeMillis())
                .transactionTime(LocalDateTime.now());
    }

    public static Store.StoreBuilder store() {
        return Store.builder()
                .businessNumber("123-45-67890")
                .storeName("테스트 매장")
                .ownerName("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .category(StoreCategory.RESTAURANT)
                .status(StoreStatus.ACTIVE);
    }
}
```

## 📚 문서화 가이드라인

### API 문서화

#### 1. OpenAPI 어노테이션

```java
@Operation(
    summary = "매출 데이터 등록",
    description = "POS 시스템에서 매출 데이터를 등록합니다. 등록된 데이터는 실시간으로 대시보드에 반영됩니다."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "매출 등록 성공",
        content = @Content(schema = @Schema(implementation = ServerApiResponse.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
})
@PostMapping("/webhook")
public ResponseEntity<ServerApiResponse<SaleResponse>> createSale(
        @Parameter(description = "매출 데이터", required = true)
        @Valid @RequestBody SaleRequest request) {
    // 구현
}
```

#### 2. 코드 주석 규칙

```java
/**
 * 매출 관리 서비스
 *
 * <p>매출 데이터의 생성, 조회, 분석 기능을 제공합니다.
 * 모든 매출 데이터는 실시간으로 메트릭에 반영되며,
 * RabbitMQ를 통해 정산 시스템으로 전송됩니다.</p>
 *
 * @author 개발팀
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SalesService {

    /**
     * 새로운 매출 데이터를 등록합니다.
     *
     * @param request 매출 등록 요청 데이터
     * @return 등록된 매출 정보
     * @throws InvalidSaleDataException 매출 데이터가 유효하지 않은 경우
     * @throws StoreNotFoundException 매장을 찾을 수 없는 경우
     */
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        // 구현
    }
}
```

### README 업데이트 규칙

1. **기능 추가 시**: 해당 기능 설명을 README의 적절한 섹션에 추가
2. **API 변경 시**: API 사용 예시 업데이트
3. **설정 변경 시**: 환경 설정 가이드 업데이트

## 👀 코드 리뷰 가이드라인

### 리뷰어 가이드

#### 1. 리뷰 우선순위

1. **기능성**: 요구사항을 올바르게 구현했는가?
2. **안전성**: 보안 취약점이나 잠재적 버그가 있는가?
3. **성능**: 성능상 문제가 될 수 있는 코드가 있는가?
4. **가독성**: 코드가 이해하기 쉽고 유지보수 가능한가?
5. **일관성**: 프로젝트의 코딩 컨벤션을 따르고 있는가?

#### 2. 리뷰 코멘트 가이드

```markdown
# ✅ 건설적인 피드백
💡 **제안**: 이 부분은 Stream API를 사용하면 더 간결하게 작성할 수 있을 것 같습니다.

⚠️ **보안 이슈**: 사용자 입력값에 대한 SQL Injection 검증이 필요해 보입니다.

🐛 **잠재적 버그**: null 체크가 누락된 것 같습니다. NPE가 발생할 수 있어요.

✨ **칭찬**: 테스트 코드가 매우 잘 작성되었네요! 엣지 케이스까지 고려해주셨군요.

# ❌ 피해야 할 코멘트
"이 코드는 틀렸습니다."
"다시 작성하세요."
"이해가 안 됩니다."
```

### PR 작성자 가이드

#### 1. 리뷰 요청 전 셀프 체크

```bash
# 코드 품질 검사
mvn clean compile
mvn test
mvn spotbugs:check

# 포맷팅 검사
mvn spring-javaformat:validate

# 테스트 커버리지 확인
mvn jacoco:report
```

#### 2. 리뷰 피드백 대응

- **즉시 수정**: 명확한 버그나 컨벤션 위반
- **토론 필요**: 설계나 구현 방식에 대한 의견 차이
- **추후 이슈**: 현재 PR 범위를 벗어나는 개선사항

## 🚨 보안 가이드라인

### 보안 체크리스트

- [ ] 사용자 입력값 검증 (SQL Injection, XSS 방지)
- [ ] 인증/인가 로직 검토
- [ ] 민감 정보 로그 출력 방지
- [ ] 환경변수로 설정값 관리
- [ ] HTTPS 사용 강제

### 취약점 신고

보안 취약점 발견 시:
1. **공개 이슈 생성 금지**
2. **이메일로 신고**: security@yourcompany.com
3. **상세한 재현 방법 포함**

---

**함께 만들어가는 더 나은 코드! 🚀**

궁금한 점이 있으시면 언제든 [GitHub Discussions](https://github.com/your-org/mini-today-sales/discussions)에서 질문해주세요!