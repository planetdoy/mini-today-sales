package com.okpos.todaysales.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component("customRabbitMQ")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final RabbitAdmin rabbitAdmin;

    @Override
    public Health health() {
        try {
            // RabbitMQ 연결 상태 확인
            Properties brokerProperties = rabbitAdmin.getQueueProperties("sales.queue");

            if (brokerProperties != null) {
                return Health.up()
                        .withDetail("status", "Connected")
                        .withDetail("broker", "RabbitMQ")
                        .withDetail("sales_queue_messages", brokerProperties.get("QUEUE_MESSAGE_COUNT"))
                        .withDetail("connection", "Active")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Queue not found")
                        .withDetail("broker", "RabbitMQ")
                        .withDetail("error", "sales.queue does not exist")
                        .build();
            }
        } catch (Exception e) {
            log.error("RabbitMQ Health Check Failed", e);
            return Health.down()
                    .withDetail("status", "Connection Failed")
                    .withDetail("broker", "RabbitMQ")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}