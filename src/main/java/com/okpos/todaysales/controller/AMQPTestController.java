package com.okpos.todaysales.controller;

import com.okpos.todaysales.service.MessagePublisherExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amqp-test")
@Tag(name = "AMQP Test", description = "AMQP Exchange 타입별 테스트 API")
public class AMQPTestController {

    private final MessagePublisherExample publisherExample;

    public AMQPTestController(MessagePublisherExample publisherExample) {
        this.publisherExample = publisherExample;
    }

    @PostMapping("/direct")
    @Operation(summary = "Direct Exchange 테스트",
        description = "라우팅 키가 정확히 일치하는 큐로 메시지 전송")
    public ResponseEntity<String> testDirectExchange() {
        publisherExample.sendDirectMessage();
        return ResponseEntity.ok("Direct Exchange 메시지 전송 완료");
    }

    @PostMapping("/topic")
    @Operation(summary = "Topic Exchange 테스트",
        description = "패턴 매칭으로 여러 큐에 메시지 라우팅")
    public ResponseEntity<String> testTopicExchange() {
        publisherExample.sendTopicMessage();
        return ResponseEntity.ok("Topic Exchange 메시지 전송 완료");
    }

    @PostMapping("/fanout")
    @Operation(summary = "Fanout Exchange 테스트",
        description = "모든 바인딩된 큐로 메시지 브로드캐스트")
    public ResponseEntity<String> testFanoutExchange() {
        publisherExample.sendFanoutMessage();
        return ResponseEntity.ok("Fanout Exchange 메시지 전송 완료");
    }
}