package com.okpos.todaysales.listener;

import com.okpos.todaysales.event.SaleCreatedEvent;
import com.okpos.todaysales.event.SettlementRequestEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SalesEventListener {
    
    private final CacheManager cacheManager;
    
    @RabbitListener(queues = "sales.queue")
    public void handleSaleCreated(
            @Payload SaleCreatedEvent event,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.info("매출 생성 이벤트 수신: saleId={}, storeId={}, amount={}", 
                    event.getSaleId(), event.getStoreId(), event.getAmount());
            
            // 대시보드 캐시 무효화
            invalidateDashboardCache(event);
            
            // 수동 ACK
            channel.basicAck(deliveryTag, false);
            log.info("매출 이벤트 처리 완료: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("매출 이벤트 처리 중 오류 발생: eventId={}", event.getEventId(), e);
            handleError(channel, deliveryTag, message, e);
        }
    }
    
    @RabbitListener(queues = "settlement.queue")
    public void handleSettlementRequest(
            @Payload SettlementRequestEvent event,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.info("정산 요청 이벤트 수신: storeId={}, date={}", 
                    event.getStoreId(), event.getSettlementDate());
            
            // 정산 처리 로직
            processSettlement(event);
            
            // 수동 ACK
            channel.basicAck(deliveryTag, false);
            log.info("정산 이벤트 처리 완료: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("정산 이벤트 처리 중 오류 발생: eventId={}", event.getEventId(), e);
            handleError(channel, deliveryTag, message, e);
        }
    }
    
    @RabbitListener(queues = "notification.queue")
    public void handleNotification(
            @Payload SaleCreatedEvent event,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.info("알림 이벤트 수신: saleId={}, storeName={}, amount={}", 
                    event.getSaleId(), event.getStoreName(), event.getAmount());
            
            // 알림 처리 (현재는 로그만)
            processNotification(event);
            
            // 수동 ACK
            channel.basicAck(deliveryTag, false);
            log.info("알림 이벤트 처리 완료: eventId={}", event.getEventId());
            
        } catch (Exception e) {
            log.error("알림 이벤트 처리 중 오류 발생: eventId={}", event.getEventId(), e);
            handleError(channel, deliveryTag, message, e);
        }
    }
    
    @RabbitListener(queues = "dlq.sales")
    public void handleSalesDLQ(
            @Payload Object payload,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.error("DLQ에서 매출 메시지 수신: {}", payload);
            
            // DLQ 메시지 처리 (로깅, 알림, 수동 처리 등)
            processDLQMessage("sales", payload, message);
            
            // 수동 ACK (DLQ에서 제거)
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류 발생", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("DLQ NACK 중 오류 발생", ioException);
            }
        }
    }
    
    @RabbitListener(queues = "dlq.settlement")
    public void handleSettlementDLQ(
            @Payload Object payload,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.error("DLQ에서 정산 메시지 수신: {}", payload);
            
            processDLQMessage("settlement", payload, message);
            
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류 발생", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("DLQ NACK 중 오류 발생", ioException);
            }
        }
    }
    
    @RabbitListener(queues = "dlq.notification")
    public void handleNotificationDLQ(
            @Payload Object payload,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel,
            Message message) {
        
        try {
            log.error("DLQ에서 알림 메시지 수신: {}", payload);
            
            processDLQMessage("notification", payload, message);
            
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 오류 발생", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("DLQ NACK 중 오류 발생", ioException);
            }
        }
    }
    
    private void invalidateDashboardCache(SaleCreatedEvent event) {
        try {
            // 오늘 날짜의 대시보드 캐시 무효화
            LocalDate today = LocalDate.now();
            String cacheKey = String.format("dashboard::%s::%s", event.getStoreId(), today);
            
            if (cacheManager.getCache("dashboards") != null) {
                cacheManager.getCache("dashboards").evict(cacheKey);
                log.info("대시보드 캐시 무효화 완료: key={}", cacheKey);
            }
            
            // 매출 날짜의 대시보드 캐시도 무효화 (과거 데이터 수정 등을 위해)
            if (event.getSaleDate() != null) {
                LocalDate saleDate = event.getSaleDate().toLocalDate();
                String saleDateCacheKey = String.format("dashboard::%s::%s", event.getStoreId(), saleDate);
                
                if (cacheManager.getCache("dashboards") != null) {
                    cacheManager.getCache("dashboards").evict(saleDateCacheKey);
                    log.info("매출 날짜 대시보드 캐시 무효화 완료: key={}", saleDateCacheKey);
                }
            }
            
        } catch (Exception e) {
            log.error("대시보드 캐시 무효화 중 오류 발생", e);
            throw e;
        }
    }
    
    private void processSettlement(SettlementRequestEvent event) {
        try {
            log.info("정산 처리 시작: storeId={}, date={}", event.getStoreId(), event.getSettlementDate());
            
            // 정산 처리 로직 구현
            // 1. 해당 날짜의 매출 데이터 조회
            // 2. 수수료 계산
            // 3. 정산 내역 생성
            // 4. 정산 파일 생성 등
            
            // 현재는 로그만 출력
            log.info("정산 처리 완료: storeId={}, date={}, status=SUCCESS", 
                    event.getStoreId(), event.getSettlementDate());
            
        } catch (Exception e) {
            log.error("정산 처리 중 오류 발생: storeId={}, date={}", 
                    event.getStoreId(), event.getSettlementDate(), e);
            throw e;
        }
    }
    
    private void processNotification(SaleCreatedEvent event) {
        try {
            // 알림 처리 로직 (현재는 로그만)
            String notificationMessage = String.format(
                "[매출 알림] %s에서 %s원의 매출이 발생했습니다. (주문번호: %s)",
                event.getStoreName(),
                event.getAmount(),
                event.getReceiptNumber()
            );
            
            log.info("알림 발송: {}", notificationMessage);
            
            // 실제 환경에서는 여기에 푸시 알림, 이메일, SMS 등의 로직 구현
            
        } catch (Exception e) {
            log.error("알림 처리 중 오류 발생", e);
            throw e;
        }
    }
    
    private void processDLQMessage(String queueType, Object payload, Message message) {
        try {
            log.error("=== DLQ 메시지 처리 ===");
            log.error("Queue Type: {}", queueType);
            log.error("Payload: {}", payload);
            log.error("Message Properties: {}", message.getMessageProperties());
            
            // DLQ 메시지 처리 로직
            // 1. 관리자에게 알림 발송
            // 2. 실패 원인 분석을 위한 데이터 저장
            // 3. 재처리가 필요한 경우 수동 처리 큐로 이동
            
            log.error("DLQ 메시지 처리 완료: queueType={}", queueType);
            
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 추가 오류 발생", e);
            throw e;
        }
    }
    
    private void handleError(Channel channel, long deliveryTag, Message message, Exception error) {
        try {
            // 재시도 횟수 확인
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }
            
            log.warn("메시지 처리 실패, 재시도 횟수: {}", retryCount);
            
            // 최대 재시도 횟수 (3회)
            if (retryCount < 3) {
                // 재시도를 위해 NACK (requeue=true)
                channel.basicNack(deliveryTag, false, true);
                log.warn("메시지 재시도 큐로 이동: retryCount={}", retryCount + 1);
            } else {
                // 최대 재시도 횟수 초과 시 NACK (requeue=false) -> DLQ로 이동
                channel.basicNack(deliveryTag, false, false);
                log.error("최대 재시도 횟수 초과, DLQ로 이동: retryCount={}", retryCount);
            }
            
        } catch (IOException e) {
            log.error("에러 처리 중 추가 오류 발생", e);
        }
    }
}