# AMQP 프로토콜 가이드

## Queue 속성

### Durability (지속성)
- **Durable**: RabbitMQ 서버 재시작 후에도 큐 유지
- **Transient**: 서버 재시작시 큐 삭제

### Exclusive (독점)
- **true**: 선언한 연결에서만 사용, 연결 종료시 큐 자동 삭제
- **false**: 여러 연결에서 공유 가능

### Auto-delete (자동 삭제)
- **true**: 마지막 consumer 연결 해제시 큐 자동 삭제
- **false**: 수동으로 삭제해야 함

## Binding (바인딩)

Queue와 Exchange를 연결하는 규칙

```java
// Direct Binding
Binding binding = BindingBuilder
    .bind(queue)
    .to(directExchange)
    .with("routing.key");

// Topic Binding with Pattern
Binding binding = BindingBuilder
    .bind(queue)
    .to(topicExchange)
    .with("order.*");  // order.created, order.updated 등 매칭

// Fanout Binding (no routing key)
Binding binding = BindingBuilder
    .bind(queue)
    .to(fanoutExchange);
```

## 메시지 라우팅 예제

### Direct Exchange
```
Producer → [Direct Exchange]
              ↓ (routing key: "email")
         [Email Queue] → Email Consumer
              ↓ (routing key: "sms")
         [SMS Queue] → SMS Consumer
```

### Topic Exchange
```
Producer → [Topic Exchange]
              ↓ (pattern: "order.*")
         [Order Queue]
              ↓ (pattern: "*.inventory")
         [Inventory Queue]
              ↓ (pattern: "#")
         [Audit Queue] (모든 메시지)
```

### Fanout Exchange
```
Producer → [Fanout Exchange]
              ↓ ↓ ↓ (모든 큐로)
    [Queue1] [Queue2] [Queue3]
```

## 실습 API

Swagger UI (http://localhost:8080/swagger-ui.html)에서 테스트:

1. **Direct Exchange 테스트**
   - POST `/api/amqp-test/direct`
   - email, sms 큐로 각각 메시지 전송

2. **Topic Exchange 테스트**
   - POST `/api/amqp-test/topic`
   - 패턴에 따라 여러 큐로 라우팅

3. **Fanout Exchange 테스트**
   - POST `/api/amqp-test/fanout`
   - 모든 notification 큐로 브로드캐스트

## RabbitMQ Management Console에서 확인

1. http://localhost:15672 접속
2. Exchanges 탭에서 각 Exchange 타입 확인
3. Queues 탭에서 메시지 수신 확인
4. Bindings 확인으로 라우팅 규칙 검증