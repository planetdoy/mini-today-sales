# Self-invocation 회피 방법 가이드

## 문제 상황
```java
@Service
public class MyService {
    @Transactional
    public void methodA() {
        methodB(); // ❌ 프록시를 거치지 않아 @Transactional 무효
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void methodB() {
        // 새 트랜잭션이 시작되지 않음!
    }
}
```

## 해결 방법들

### 1. ApplicationContext 사용
```java
@Service
public class SettlementBatchService {
    @Autowired
    private ApplicationContext applicationContext;

    public void methodA() {
        // Self-invocation 회피
        SettlementBatchService self = applicationContext.getBean(SettlementBatchService.class);
        self.methodB(); // ✅ 프록시를 통한 호출
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void methodB() {
        // 새 트랜잭션 정상 동작
    }
}
```

### 2. @Lazy + Self-injection
```java
@Service
public class MyService {
    @Lazy
    @Autowired
    private MyService self;

    @Transactional
    public void methodA() {
        self.methodB(); // ✅ 프록시를 통한 호출
    }
}
```

### 3. AopContext 사용
```java
@Service
@EnableAspectJAutoProxy(exposeProxy = true)
public class MyService {
    @Transactional
    public void methodA() {
        MyService self = (MyService) AopContext.currentProxy();
        self.methodB(); // ✅ 프록시를 통한 호출
    }
}
```

### 4. 별도 서비스 분리 (권장 ⭐)
```java
@Service
public class SettlementBatchService {
    private final SettlementFailureService failureService;

    public void processSettlement(LocalDate date) {
        try {
            processSettlementInTransaction(date); // 같은 클래스 내 메서드
        } catch (Exception e) {
            failureService.saveFailure(date, e.getMessage()); // ✅ 별도 서비스 호출
        }
    }

    @Transactional
    public Settlement processSettlementInTransaction(LocalDate date) {
        // 메인 정산 로직
    }
}

@Service
public class SettlementFailureService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(LocalDate date, String error) {
        // 실패 처리 로직 - 독립적인 트랜잭션
    }
}
```

## 방법별 비교

| 방법 | 장점 | 단점 | 권장도 |
|------|------|------|--------|
| ApplicationContext | 간단한 구현 | 약간의 성능 오버헤드 | ⭐⭐⭐ |
| Self-injection | 깔끔한 코드 | 순환 참조 위험 | ⭐⭐ |
| AopContext | 표준 방법 | 설정 복잡 | ⭐⭐ |
| 서비스 분리 | 명확한 책임 분리 | 클래스 증가 | ⭐⭐⭐⭐⭐ |

## 우리 프로젝트 적용 사례

### 초기 구현 (ApplicationContext 방식)
```java
@Service
public class SettlementBatchService {
    @Autowired
    private ApplicationContext applicationContext;

    public Settlement processSettlement(LocalDate date) {
        try {
            return processSettlementInTransaction(date);
        } catch (Exception e) {
            // Self-invocation 회피
            SettlementBatchService self = applicationContext.getBean(SettlementBatchService.class);
            self.saveFailedSettlement(date, e.getMessage());
            throw e;
        }
    }
}
```

### 개선된 구현 (서비스 분리 방식) ⭐ 최종 적용
```java
@Service
public class SettlementBatchService {
    private final SettlementFailureService settlementFailureService;

    public Settlement processSettlement(LocalDate date) {
        try {
            return processSettlementInTransaction(date);
        } catch (Exception e) {
            // 별도 서비스로 실패 처리 - Self-invocation 문제 없음
            settlementFailureService.saveFailedSettlement(date, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Settlement processSettlementInTransaction(LocalDate date) {
        // 정산 로직
    }
}

@Service
public class SettlementFailureService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedSettlement(LocalDate date, String error) {
        // 실패 처리 로직
    }
}
```

### 선택 이유 변경사항

**처음 선택**: ApplicationContext 방식
- 빠른 구현
- 기존 코드 변경 최소화

**최종 선택**: 서비스 분리 방식
1. **단일 책임 원칙**: 각 서비스가 명확한 역할
2. **테스트 용이성**: 개별 서비스 단위 테스트 가능
3. **재사용성**: FailureService를 다른 배치에서도 활용
4. **확장성**: 실패 처리 로직 추가 시 한 곳에서 관리
5. **Self-invocation 완전 해결**: 별도 서비스 호출로 근본 해결

## 핵심 학습 포인트

### 1. 트랜잭션 전파 속성의 중요성
```java
// 메인 트랜잭션이 롤백되어도
@Transactional
public void mainProcess() {
    // 실패 시 롤백
}

// 별도 트랜잭션은 커밋됨
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveFailure() {
    // 실패 기록은 보존
}
```

### 2. 서비스 분리의 장점
- **명확한 책임**: 각 서비스가 하나의 목적
- **독립적 테스트**: Mock 주입으로 단위 테스트
- **재사용 가능**: 다른 배치 작업에서 활용
- **확장 용이**: 새 기능 추가 시 한 곳에서 관리

## 주의사항
- Self-invocation은 `@Transactional` 뿐만 아니라 모든 AOP 기능에서 발생
- `@Async`, `@Cacheable`, `@Secured` 등도 동일한 문제 발생
- 프록시 모드에서만 발생 (AspectJ 위빙 모드에서는 발생하지 않음)
- **권장**: 복잡한 비즈니스 로직은 처음부터 서비스를 분리하여 설계