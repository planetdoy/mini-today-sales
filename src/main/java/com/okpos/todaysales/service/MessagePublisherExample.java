package com.okpos.todaysales.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisherExample {

    private final RabbitTemplate rabbitTemplate;

    public MessagePublisherExample(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Direct Exchange 예제
    public void sendDirectMessage() {
        // email 큐로만 전달
        rabbitTemplate.convertAndSend("example.direct", "email",
            "이메일 알림: 주문이 완료되었습니다");

        // sms 큐로만 전달
        rabbitTemplate.convertAndSend("example.direct", "sms",
            "SMS 알림: 주문번호 #12345");

        // 매칭되는 큐가 없으면 메시지 손실
        rabbitTemplate.convertAndSend("example.direct", "push",
            "푸시 알림 (전달 안됨)");
    }

    // Topic Exchange 예제
    public void sendTopicMessage() {
        // order.queue로 전달 (order.* 패턴 매칭)
        rabbitTemplate.convertAndSend("example.topic", "order.created",
            "새 주문이 생성되었습니다");

        // inventory.queue로 전달 (*.inventory 패턴 매칭)
        rabbitTemplate.convertAndSend("example.topic", "product.inventory",
            "재고가 업데이트되었습니다");

        // audit.queue로만 전달 (# 패턴은 모든 것 매칭)
        rabbitTemplate.convertAndSend("example.topic", "user.login",
            "사용자 로그인 이벤트");

        // order.queue와 audit.queue로 전달
        rabbitTemplate.convertAndSend("example.topic", "order.cancelled",
            "주문이 취소되었습니다");
    }

    // Fanout Exchange 예제
    public void sendFanoutMessage() {
        // 모든 바인딩된 큐(notification.queue1, 2, 3)로 전달
        // 라우팅 키는 무시됨
        rabbitTemplate.convertAndSend("example.fanout", "",
            "전체 공지: 시스템 점검이 있을 예정입니다");

        rabbitTemplate.convertAndSend("example.fanout", "ignored.key",
            "라우팅 키가 있어도 모든 큐로 전달됩니다");
    }

    // Queue 속성 예제
    public void demonstrateQueueProperties() {
        // Durable Queue: 서버 재시작 후에도 유지
        // Non-durable Queue: 서버 재시작시 삭제

        // Exclusive Queue: 선언한 연결에서만 사용, 연결 종료시 삭제
        // Auto-delete Queue: 모든 consumer가 없어지면 자동 삭제

        // Arguments 예제:
        // x-message-ttl: 메시지 만료 시간
        // x-max-length: 큐 최대 길이
        // x-dead-letter-exchange: DLX 지정
        // x-max-priority: 우선순위 큐 설정
    }
}