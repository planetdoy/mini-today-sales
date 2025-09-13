package com.okpos.todaysales.event;

import com.okpos.todaysales.config.RabbitMQConfig;
import com.okpos.todaysales.entity.Sale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishSaleCreated(Sale sale) {
        try {
            SaleCreatedEvent event = SaleCreatedEvent.from(
                sale.getId(),
                sale.getStore().getId(),
                sale.getStore().getStoreName(),
                "POS-001", // 기본값
                sale.getOrderNumber(),
                sale.getAmount(),
                sale.getPaymentType().name(),
                sale.getStatus().name(),
                sale.getTransactionTime(),
                "SYSTEM" // 기본값
            );
            
            log.info("Publishing SaleCreatedEvent: saleId={}, storeId={}, amount={}", 
                    event.getSaleId(), event.getStoreId(), event.getAmount());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_EXCHANGE,
                RabbitMQConfig.SALES_ROUTING_KEY,
                event
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                event
            );
            
            log.info("Successfully published SaleCreatedEvent: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish SaleCreatedEvent for sale: {}", sale.getId(), e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
    
    public void publishSettlementRequest(Long storeId, LocalDate date) {
        try {
            SettlementRequestEvent event = SettlementRequestEvent.simpleCreate(storeId, date);
            
            log.info("Publishing SettlementRequestEvent: storeId={}, date={}", 
                    event.getStoreId(), event.getSettlementDate());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_EXCHANGE,
                RabbitMQConfig.SETTLEMENT_ROUTING_KEY,
                event
            );
            
            log.info("Successfully published SettlementRequestEvent: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish SettlementRequestEvent for storeId: {}, date: {}", 
                     storeId, date, e);
            throw new RuntimeException("Settlement event publishing failed", e);
        }
    }
    
    public void publishSettlementRequest(Long storeId, String storeName, LocalDate settlementDate,
                                       BigDecimal totalAmount, Long transactionCount,
                                       Map<String, BigDecimal> paymentBreakdown,
                                       Map<String, Long> paymentCounts, String requestedBy) {
        try {
            SettlementRequestEvent event = SettlementRequestEvent.create(
                storeId, storeName, settlementDate, totalAmount, transactionCount,
                paymentBreakdown, paymentCounts, requestedBy
            );
            
            log.info("Publishing detailed SettlementRequestEvent: storeId={}, date={}, amount={}, count={}", 
                    event.getStoreId(), event.getSettlementDate(), event.getTotalAmount(), event.getTransactionCount());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_EXCHANGE,
                RabbitMQConfig.SETTLEMENT_ROUTING_KEY,
                event
            );
            
            log.info("Successfully published detailed SettlementRequestEvent: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish detailed SettlementRequestEvent for storeId: {}, date: {}", 
                     storeId, settlementDate, e);
            throw new RuntimeException("Detailed settlement event publishing failed", e);
        }
    }
    
    public void publishSaleCreatedForNotification(Sale sale) {
        try {
            SaleCreatedEvent event = SaleCreatedEvent.from(
                sale.getId(),
                sale.getStore().getId(),
                sale.getStore().getStoreName(),
                "POS-001", // 기본값
                sale.getOrderNumber(),
                sale.getAmount(),
                sale.getPaymentType().name(),
                sale.getStatus().name(),
                sale.getTransactionTime(),
                "SYSTEM" // 기본값
            );
            
            log.info("Publishing SaleCreatedEvent for notification: saleId={}", sale.getId());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SALES_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                event
            );
            
        } catch (Exception e) {
            log.warn("Failed to publish notification event for sale: {}", sale.getId(), e);
        }
    }
}