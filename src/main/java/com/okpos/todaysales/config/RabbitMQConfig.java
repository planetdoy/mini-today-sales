package com.okpos.todaysales.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    
    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int port;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String password;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;
    
    public static final String SALES_EXCHANGE = "sales.exchange";
    public static final String SALES_QUEUE = "sales.queue";
    public static final String SETTLEMENT_QUEUE = "settlement.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLQ_SALES = "dlq.sales";
    public static final String DLQ_SETTLEMENT = "dlq.settlement";
    public static final String DLQ_NOTIFICATION = "dlq.notification";
    
    public static final String SALES_ROUTING_KEY = "sales.created";
    public static final String SETTLEMENT_ROUTING_KEY = "sales.settlement";
    public static final String NOTIFICATION_ROUTING_KEY = "sales.notification";
    
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        connectionFactory.setChannelCacheSize(25);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }
    
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);
        
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message failed to send: " + cause);
            }
        });
        
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage() + 
                             ", ReplyCode: " + returned.getReplyCode() + 
                             ", ReplyText: " + returned.getReplyText());
        });
        
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
    
    @Bean
    public TopicExchange salesExchange() {
        return ExchangeBuilder
                .topicExchange(SALES_EXCHANGE)
                .durable(true)
                .build();
    }
    
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }
    
    @Bean
    public Queue salesQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.sales");
        args.put("x-message-ttl", 86400000);
        args.put("x-max-length", 10000);
        
        return QueueBuilder
                .durable(SALES_QUEUE)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Queue settlementQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.settlement");
        args.put("x-message-ttl", 86400000);
        args.put("x-max-length", 10000);
        
        return QueueBuilder
                .durable(SETTLEMENT_QUEUE)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", "dlq.notification");
        args.put("x-message-ttl", 86400000);
        args.put("x-max-length", 10000);
        
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Queue dlqSales() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 604800000);
        args.put("x-max-length", 1000);
        
        return QueueBuilder
                .durable(DLQ_SALES)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Queue dlqSettlement() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 604800000);
        args.put("x-max-length", 1000);
        
        return QueueBuilder
                .durable(DLQ_SETTLEMENT)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Queue dlqNotification() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 604800000);
        args.put("x-max-length", 1000);
        
        return QueueBuilder
                .durable(DLQ_NOTIFICATION)
                .withArguments(args)
                .build();
    }
    
    @Bean
    public Binding salesBinding() {
        return BindingBuilder
                .bind(salesQueue())
                .to(salesExchange())
                .with(SALES_ROUTING_KEY);
    }
    
    @Bean
    public Binding settlementBinding() {
        return BindingBuilder
                .bind(settlementQueue())
                .to(salesExchange())
                .with(SETTLEMENT_ROUTING_KEY);
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(salesExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }
    
    @Bean
    public Binding dlqSalesBinding() {
        return BindingBuilder
                .bind(dlqSales())
                .to(deadLetterExchange())
                .with("dlq.sales");
    }
    
    @Bean
    public Binding dlqSettlementBinding() {
        return BindingBuilder
                .bind(dlqSettlement())
                .to(deadLetterExchange())
                .with("dlq.settlement");
    }
    
    @Bean
    public Binding dlqNotificationBinding() {
        return BindingBuilder
                .bind(dlqNotification())
                .to(deadLetterExchange())
                .with("dlq.notification");
    }
}