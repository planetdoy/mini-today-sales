package com.okpos.todaysales.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeExampleConfig {

    // ============= Direct Exchange 예제 =============
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("example.direct");
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("email.queue");
    }

    @Bean
    public Queue smsQueue() {
        return new Queue("sms.queue");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
            .bind(emailQueue())
            .to(directExchange())
            .with("email");  // 라우팅 키가 "email"인 메시지만 받음
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder
            .bind(smsQueue())
            .to(directExchange())
            .with("sms");  // 라우팅 키가 "sms"인 메시지만 받음
    }

    // ============= Topic Exchange 예제 =============
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("example.topic");
    }

    @Bean
    public Queue orderQueue() {
        return new Queue("order.queue");
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue("inventory.queue");
    }

    @Bean
    public Queue auditQueue() {
        return new Queue("audit.queue");
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder
            .bind(orderQueue())
            .to(topicExchange())
            .with("order.*");  // order.created, order.updated 등 매칭
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
            .bind(inventoryQueue())
            .to(topicExchange())
            .with("*.inventory");  // product.inventory, warehouse.inventory 등 매칭
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
            .bind(auditQueue())
            .to(topicExchange())
            .with("#");  // 모든 메시지 매칭 (감사 로그용)
    }

    // ============= Fanout Exchange 예제 =============
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("example.fanout");
    }

    @Bean
    public Queue notificationQueue1() {
        return new Queue("notification.queue1");
    }

    @Bean
    public Queue notificationQueue2() {
        return new Queue("notification.queue2");
    }

    @Bean
    public Queue notificationQueue3() {
        return new Queue("notification.queue3");
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder
            .bind(notificationQueue1())
            .to(fanoutExchange());  // 라우팅 키 없음
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder
            .bind(notificationQueue2())
            .to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding3() {
        return BindingBuilder
            .bind(notificationQueue3())
            .to(fanoutExchange());
    }
}