# 📚 API 문서 (API Documentation)

Mini Today Sales 시스템의 전체 REST API 명세서입니다.

## 📋 목차

- [개요](#-개요)
- [인증 및 권한](#-인증-및-권한)
- [공통 응답 형식](#-공통-응답-형식)
- [에러 코드 정리](#-에러-코드-정리)
- [매출 관리 API](#-매출-관리-api)
- [정산 관리 API](#-정산-관리-api)
- [모니터링 API](#-모니터링-api)
- [AMQP 테스트 API](#-amqp-테스트-api)
- [DLQ 모니터링 API](#-dlq-모니터링-api)
- [율제한 및 제한사항](#-율제한-및-제한사항)

## 🎯 개요

### Base URL
```
Development: http://localhost:8080
Production: https://api.yourdomain.com
```

### API 버전
- **Current Version**: v1
- **API Prefix**: `/api/v1`

### Content Type
- **Request**: `application/json`
- **Response**: `application/json`

### HTTP 상태 코드

| 상태 코드 | 의미 | 설명 |
|-----------|------|------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 리소스 충돌 |
| 422 | Unprocessable Entity | 유효성 검증 실패 |
| 500 | Internal Server Error | 서버 내부 오류 |

## 🔐 인증 및 권한

### API Key 인증 (예정)
```bash
# Header에 API Key 포함
curl -H "X-API-Key: your-api-key" \
     -H "Content-Type: application/json" \
     "https://api.yourdomain.com/api/v1/sales"
```

### JWT 토큰 인증 (예정)
```bash
# Authorization Header에 Bearer 토큰 포함
curl -H "Authorization: Bearer your-jwt-token" \
     -H "Content-Type: application/json" \
     "https://api.yourdomain.com/api/v1/sales"
```

## 📝 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다",
  "data": {
    // 실제 데이터
  },
  "timestamp": 1705123456789
}
```

### 에러 응답
```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "입력 데이터가 유효하지 않습니다",
  "path": "/api/v1/sales/webhook",
  "timestamp": "2024-01-15T14:30:00",
  "validationErrors": {
    "businessNumber": "사업자번호 형식이 올바르지 않습니다",
    "amount": "금액은 0보다 커야 합니다"
  }
}
```

## ❌ 에러 코드 정리

### 일반 에러 (General Errors)

| 에러 코드 | HTTP 상태 | 설명 | 해결 방법 |
|-----------|-----------|------|-----------|
| `VALIDATION_ERROR` | 400 | 요청 데이터 유효성 검증 실패 | 요청 데이터 형식 확인 |
| `INVALID_REQUEST` | 400 | 잘못된 요청 형식 | API 명세 확인 |
| `UNAUTHORIZED` | 401 | 인증 실패 | 인증 토큰 확인 |
| `FORBIDDEN` | 403 | 권한 없음 | 권한 설정 확인 |
| `RESOURCE_NOT_FOUND` | 404 | 요청한 리소스 없음 | 요청 경로 및 ID 확인 |
| `METHOD_NOT_ALLOWED` | 405 | 허용되지 않은 HTTP 메서드 | HTTP 메서드 확인 |
| `CONFLICT` | 409 | 리소스 충돌 | 중복 데이터 확인 |
| `RATE_LIMIT_EXCEEDED` | 429 | 요청 한도 초과 | 요청 빈도 조절 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 | 서버 로그 확인 |

### 매출 관련 에러 (Sales Errors)

| 에러 코드 | HTTP 상태 | 설명 | 해결 방법 |
|-----------|-----------|------|-----------|
| `STORE_NOT_FOUND` | 404 | 가맹점을 찾을 수 없음 | 사업자번호 확인 |
| `DUPLICATE_ORDER_NUMBER` | 409 | 중복된 주문번호 | 고유한 주문번호 사용 |
| `INVALID_PAYMENT_TYPE` | 400 | 지원하지 않는 결제 수단 | 지원 결제 수단 확인 |
| `INVALID_SALE_AMOUNT` | 400 | 유효하지 않은 매출 금액 | 양수 금액 입력 |
| `INVALID_BUSINESS_NUMBER` | 400 | 잘못된 사업자번호 형식 | xxx-xx-xxxxx 형식 사용 |
| `SALE_PROCESSING_ERROR` | 500 | 매출 처리 중 오류 | 관리자 문의 |

### 정산 관련 에러 (Settlement Errors)

| 에러 코드 | HTTP 상태 | 설명 | 해결 방법 |
|-----------|-----------|------|-----------|
| `SETTLEMENT_NOT_FOUND` | 404 | 정산 정보를 찾을 수 없음 | 정산 ID 또는 날짜 확인 |
| `SETTLEMENT_ALREADY_EXISTS` | 409 | 이미 정산된 날짜 | 다른 날짜 선택 |
| `SETTLEMENT_IN_PROGRESS` | 409 | 정산 처리 중 | 처리 완료 후 재시도 |
| `NO_SALES_TO_SETTLE` | 400 | 정산할 매출이 없음 | 매출 데이터 확인 |
| `SETTLEMENT_CALCULATION_ERROR` | 500 | 정산 계산 오류 | 관리자 문의 |

### 모니터링 관련 에러 (Monitoring Errors)

| 에러 코드 | HTTP 상태 | 설명 | 해결 방법 |
|-----------|-----------|------|-----------|
| `METRIC_NOT_FOUND` | 404 | 요청한 메트릭을 찾을 수 없음 | 메트릭 이름 확인 |
| `MONITORING_SERVICE_UNAVAILABLE` | 503 | 모니터링 서비스 불가 | 서비스 상태 확인 |

## 💰 매출 관리 API

### 1. 매출 등록 (Webhook)

매출 데이터를 시스템에 등록합니다.

```http
POST /api/v1/sales/webhook
```

#### Request Body
```json
{
  "businessNumber": "123-45-67890",
  "transactionTime": "2024-01-15T14:30:00",
  "amount": 25000,
  "paymentType": "CARD",
  "channel": "ONLINE",
  "orderNumber": "ORDER-20240115-001"
}
```

#### Parameters

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| `businessNumber` | string | ✓ | 사업자번호 (xxx-xx-xxxxx) | "123-45-67890" |
| `transactionTime` | datetime | ✓ | 거래 시간 (ISO 8601) | "2024-01-15T14:30:00" |
| `amount` | decimal | ✓ | 결제 금액 (최소 0.01) | 25000 |
| `paymentType` | enum | ✓ | 결제 수단 (CARD, CASH) | "CARD" |
| `channel` | enum | ✓ | 판매 채널 (ONLINE, OFFLINE) | "ONLINE" |
| `orderNumber` | string | ✓ | 주문번호 (최대 50자, 고유값) | "ORDER-20240115-001" |

#### Response (201 Created)
```json
{
  "success": true,
  "message": "매출이 성공적으로 등록되었습니다",
  "data": {
    "id": 1,
    "businessNumber": "123-45-67890",
    "amount": 25000,
    "paymentType": "CARD",
    "channel": "ONLINE",
    "orderNumber": "ORDER-20240115-001",
    "status": "COMPLETED",
    "fee": 500,
    "netAmount": 24500,
    "isSettled": false,
    "createdAt": "2024-01-15T14:30:00"
  }
}
```

#### 가능한 에러
- `400 VALIDATION_ERROR`: 요청 데이터 검증 실패
- `404 STORE_NOT_FOUND`: 사업자번호에 해당하는 가맹점 없음
- `409 DUPLICATE_ORDER_NUMBER`: 중복된 주문번호
- `500 SALE_PROCESSING_ERROR`: 매출 처리 중 서버 오류

### 2. 대시보드 조회

특정 날짜의 매출 대시보드 데이터를 조회합니다.

```http
GET /api/v1/sales/dashboard/{businessNumber}?date=2024-01-15
```

#### Path Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `businessNumber` | string | ✓ | 사업자번호 |

#### Query Parameters
| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|-------|------|
| `date` | date | | 오늘 | 조회할 날짜 (YYYY-MM-DD) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "date": "2024-01-15",
    "totalAmount": 125000,
    "totalCount": 15,
    "paymentTypeStatistics": [
      {
        "paymentType": "CARD",
        "amount": 100000,
        "count": 12,
        "fee": 2000,
        "netAmount": 98000
      },
      {
        "paymentType": "CASH",
        "amount": 25000,
        "count": 3,
        "fee": 0,
        "netAmount": 25000
      }
    ],
    "hourlyStatistics": [
      {
        "hour": 14,
        "amount": 25000,
        "count": 3
      },
      {
        "hour": 15,
        "amount": 50000,
        "count": 5
      }
    ]
  }
}
```

### 3. 매출 목록 조회

지정된 기간의 매출 목록을 페이징하여 조회합니다.

```http
GET /api/v1/sales/{businessNumber}?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&page=0&size=20
```

#### Path Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `businessNumber` | string | ✓ | 사업자번호 |

#### Query Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `startDate` | datetime | ✓ | 시작 일시 (ISO 8601) |
| `endDate` | datetime | ✓ | 종료 일시 (ISO 8601) |
| `page` | integer | | 페이지 번호 (0부터 시작) |
| `size` | integer | | 페이지 크기 (기본 20) |
| `sort` | string | | 정렬 기준 (예: transactionTime,desc) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "amount": 25000,
        "paymentType": "CARD",
        "channel": "ONLINE",
        "orderNumber": "ORDER-001",
        "transactionTime": "2024-01-15T14:30:00",
        "status": "COMPLETED"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "ascending": false
      }
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 4. 월별 리포트 조회

특정 월의 상세한 매출 리포트를 조회합니다.

```http
GET /api/v1/sales/report/monthly/{businessNumber}?yearMonth=2024-01
```

#### Path Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `businessNumber` | string | ✓ | 사업자번호 |

#### Query Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `yearMonth` | string | ✓ | 년월 (YYYY-MM) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "yearMonth": "2024-01",
    "businessNumber": "123-45-67890",
    "totalAmount": 3250000,
    "totalCount": 156,
    "totalFee": 65000,
    "totalNetAmount": 3185000,
    "dailyStatistics": [
      {
        "day": 1,
        "amount": 105000,
        "count": 8
      }
    ],
    "paymentTypeStatistics": [
      {
        "paymentType": "CARD",
        "amount": 2600000,
        "count": 125,
        "fee": 52000,
        "netAmount": 2548000
      }
    ]
  }
}
```

## 🧾 정산 관리 API

### 1. 수동 정산 실행

특정 날짜의 매출을 수동으로 정산합니다.

```http
POST /api/settlements/manual?settlementDate=2024-01-15
```

#### Query Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `settlementDate` | date | ✓ | 정산할 날짜 (YYYY-MM-DD) |

#### Response (200 OK)
```json
{
  "id": 1,
  "settlementDate": "2024-01-15",
  "totalAmount": 125000,
  "totalFee": 2500,
  "settlementAmount": 122500,
  "status": "COMPLETED",
  "processedAt": "2024-01-16T01:00:00",
  "salesCount": 15
}
```

#### 가능한 에러
- `409 SETTLEMENT_ALREADY_EXISTS`: 이미 정산된 날짜
- `400 NO_SALES_TO_SETTLE`: 정산할 매출이 없음

### 2. 정산 조회

정산 ID로 정산 정보를 조회합니다.

```http
GET /api/settlements/{id}
```

#### Path Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | long | ✓ | 정산 ID |

#### Response (200 OK)
```json
{
  "id": 1,
  "settlementDate": "2024-01-15",
  "totalAmount": 125000,
  "totalFee": 2500,
  "settlementAmount": 122500,
  "status": "COMPLETED",
  "processedAt": "2024-01-16T01:00:00",
  "salesCount": 15,
  "store": {
    "businessNumber": "123-45-67890",
    "storeName": "테스트 매장"
  }
}
```

### 3. 날짜별 정산 조회

특정 날짜의 정산 정보를 조회합니다.

```http
GET /api/settlements/date/{date}
```

#### Path Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `date` | date | ✓ | 정산 날짜 (YYYY-MM-DD) |

### 4. 기간별 정산 목록 조회

특정 기간의 정산 목록을 조회합니다.

```http
GET /api/settlements/range?startDate=2024-01-01&endDate=2024-01-31
```

#### Query Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `startDate` | date | ✓ | 시작 날짜 |
| `endDate` | date | ✓ | 종료 날짜 |

### 5. 미정산 매출 조회

특정 날짜의 미정산 매출 목록을 조회합니다.

```http
GET /api/settlements/unsettled/{date}
```

### 6. 정산 재처리

실패한 정산을 재처리합니다.

```http
POST /api/settlements/{id}/reprocess
```

### 7. 정산 여부 확인

특정 날짜가 이미 정산되었는지 확인합니다.

```http
GET /api/settlements/check/{date}
```

#### Response (200 OK)
```json
{
  "exists": true
}
```

## 📊 모니터링 API

### 1. 시스템 헬스 체크

전체 시스템의 상태를 확인합니다.

```http
GET /api/monitoring/health
```

#### Response (200 OK)
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "rabbit": {
      "status": "UP",
      "details": {
        "version": "3.12.0"
      }
    }
  }
}
```

### 2. 주요 메트릭 요약

핵심 비즈니스 메트릭을 요약해서 조회합니다.

```http
GET /api/monitoring/metrics/summary
```

#### Response (200 OK)
```json
{
  "total_sales_count": 1563.0,
  "total_sales_amount": 45230000.0,
  "settlement_success_count": 30.0,
  "settlement_failure_count": 2.0,
  "total_api_calls": 5487.0,
  "avg_settlement_time": 245.6
}
```

### 3. 커스텀 메트릭 조회

특정 메트릭의 상세 정보를 조회합니다.

```http
GET /api/monitoring/metrics/custom?metricName=sales.created.total&tag=paymentType:CARD
```

#### Query Parameters
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `metricName` | string | ✓ | 메트릭 이름 |
| `tag` | string | | 태그 필터 (key:value 형식) |

### 4. Prometheus 메트릭

Prometheus 형식의 메트릭을 조회합니다.

```http
GET /api/monitoring/prometheus
```

#### Response (200 OK)
```
# HELP sales_created_total Total number of sales created
# TYPE sales_created_total counter
sales_created_total{paymentType="CARD"} 1203.0
sales_created_total{paymentType="CASH"} 360.0

# HELP jvm_memory_used_bytes Used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 123456789.0
```

### 5. 시스템 정보

시스템 운영 정보를 조회합니다.

```http
GET /api/monitoring/system/info
```

#### Response (200 OK)
```json
{
  "jvm_memory_total": 1073741824,
  "jvm_memory_free": 536870912,
  "jvm_memory_used": 536870912,
  "jvm_memory_max": 2147483648,
  "processors": 8,
  "java_version": "11.0.16",
  "os_name": "Linux",
  "uptime": 1705123456789
}
```

## 📨 AMQP 테스트 API

### 1. 테스트 메시지 발송

RabbitMQ 테스트 메시지를 발송합니다.

```http
POST /api/amqp/test/send
```

#### Request Body
```json
{
  "message": "테스트 메시지",
  "routingKey": "test.routing.key"
}
```

### 2. 배치 메시지 발송

여러 개의 테스트 메시지를 일괄 발송합니다.

```http
POST /api/amqp/test/batch?count=10
```

### 3. 지연 메시지 발송

지정된 시간 후에 메시지를 발송합니다.

```http
POST /api/amqp/test/delayed?delaySeconds=30
```

## 🔍 DLQ 모니터링 API

### 1. DLQ 상태 조회

Dead Letter Queue의 현재 상태를 조회합니다.

```http
GET /api/monitoring/dlq/status
```

#### Response (200 OK)
```json
{
  "totalMessages": 5,
  "unprocessedMessages": 3,
  "processingMessages": 1,
  "failedMessages": 1,
  "lastProcessedAt": "2024-01-15T14:30:00",
  "queueHealth": "WARNING"
}
```

### 2. DLQ 메시지 목록 조회

DLQ에 있는 메시지 목록을 조회합니다.

```http
GET /api/monitoring/dlq/messages?page=0&size=10
```

### 3. DLQ 메시지 재처리

특정 DLQ 메시지를 재처리합니다.

```http
POST /api/monitoring/dlq/reprocess/{messageId}
```

### 4. DLQ 통계 조회

DLQ 관련 통계 정보를 조회합니다.

```http
GET /api/monitoring/dlq/statistics
```

#### Response (200 OK)
```json
{
  "dailyFailureCount": 15,
  "hourlyFailureRate": 2.3,
  "topFailureReasons": [
    {
      "reason": "CONNECTION_TIMEOUT",
      "count": 8
    },
    {
      "reason": "VALIDATION_ERROR",
      "count": 5
    }
  ],
  "averageRetryAttempts": 3.2
}
```

## ⚡ 율제한 및 제한사항

### 요청 제한 (Rate Limiting)

| API 그룹 | 제한 | 설명 |
|----------|------|------|
| 매출 등록 | 1,000 req/min | 매출 데이터 등록 |
| 조회 API | 5,000 req/min | 대시보드, 리포트 조회 |
| 정산 API | 100 req/min | 정산 관련 작업 |
| 모니터링 | 500 req/min | 메트릭, 헬스체크 |

### 페이징 제한

| 항목 | 제한값 | 설명 |
|------|--------|------|
| 최대 페이지 크기 | 100 | 한 번에 조회할 수 있는 최대 항목 수 |
| 기본 페이지 크기 | 20 | 기본 페이지 크기 |
| 최대 조회 기간 | 1년 | 한 번에 조회할 수 있는 최대 기간 |

### 데이터 제한

| 항목 | 제한값 | 설명 |
|------|--------|------|
| 주문번호 길이 | 50자 | 주문번호 최대 길이 |
| 매출 금액 | 1억원 | 단일 매출 최대 금액 |
| 파일 업로드 | 10MB | 업로드 파일 최대 크기 |

## 🔧 개발자 도구

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### API 테스트
```bash
# 매출 등록 테스트
curl -X POST http://localhost:8080/api/v1/sales/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "businessNumber": "123-45-67890",
    "transactionTime": "2024-01-15T14:30:00",
    "amount": 25000,
    "paymentType": "CARD",
    "channel": "ONLINE",
    "orderNumber": "ORDER-TEST-001"
  }'

# 헬스체크
curl http://localhost:8080/api/monitoring/health

# 메트릭 조회
curl http://localhost:8080/api/monitoring/metrics/summary
```

### Postman Collection
프로젝트 루트의 `postman/` 디렉토리에서 Postman 컬렉션을 확인할 수 있습니다.

## 📞 지원 및 문의

- **API 문의**: api-support@yourcompany.com
- **기술 지원**: tech-support@yourcompany.com
- **버그 리포트**: [GitHub Issues](https://github.com/your-org/mini-today-sales/issues)

---

**API 문서 버전**: v1.0.0
**마지막 업데이트**: 2024-01-15
**다음 업데이트 예정**: 2024-02-01

> 💡 **팁**: API 사용 중 문제가 발생하면 먼저 헬스체크 엔드포인트로 시스템 상태를 확인해보세요!