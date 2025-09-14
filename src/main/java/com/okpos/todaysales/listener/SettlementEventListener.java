package com.okpos.todaysales.listener;

import com.okpos.todaysales.dto.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEventListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RabbitListener(queues = "settlement.queue")
    public void handleSettlementEvent(SettlementEvent event) {
        log.info("정산 이벤트 수신: {}", event);

        try {
            if ("COMPLETED".equals(event.getStatus())) {
                processCompletedSettlement(event);
            } else if ("FAILED".equals(event.getStatus())) {
                processFailedSettlement(event);
            }
        } catch (Exception e) {
            log.error("정산 이벤트 처리 중 오류: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "notification.queue")
    public void handleNotificationEvent(String message) {
        log.info("알림 이벤트 수신: {}", message);

        try {
            // 실제 환경에서는 이메일, SMS, Slack 등으로 알림 발송
            sendNotification(message);
        } catch (Exception e) {
            log.error("알림 발송 중 오류: {}", message, e);
        }
    }

    private void processCompletedSettlement(SettlementEvent event) {
        String message = String.format(
                "정산 완료\n" +
                "=============================\n" +
                "정산 ID: %d\n" +
                "정산 날짜: %s\n" +
                "거래 건수: %d건\n" +
                "총 매출액: %,.0f원\n" +
                "총 수수료: %,.0f원\n" +
                "정산 금액: %,.0f원\n" +
                "처리 시간: %s\n" +
                "=============================",
                event.getSettlementId(),
                event.getSettlementDate().format(DATE_FORMATTER),
                event.getTransactionCount(),
                event.getTotalAmount(),
                event.getTotalFee(),
                event.getNetAmount(),
                event.getProcessedAt().format(DATETIME_FORMATTER)
        );

        log.info(message);
        sendNotification(message);

        // 추가 처리 로직
        // - 정산 보고서 생성
        // - 회계 시스템 연동
        // - 대시보드 업데이트
    }

    private void processFailedSettlement(SettlementEvent event) {
        String message = String.format(
                "⚠️ 정산 실패 알림 ⚠️\n" +
                "=============================\n" +
                "정산 날짜: %s\n" +
                "오류 메시지: %s\n" +
                "발생 시간: %s\n" +
                "=============================\n" +
                "관리자 확인이 필요합니다.",
                event.getSettlementDate().format(DATE_FORMATTER),
                event.getMessage() != null ? event.getMessage() : "알 수 없는 오류",
                event.getProcessedAt().format(DATETIME_FORMATTER)
        );

        log.error(message);
        sendUrgentNotification(message);
    }

    private void sendNotification(String message) {
        // 일반 알림 발송
        log.info("일반 알림 발송: {}", message);

        // 실제 구현 시:
        // - 이메일 발송
        // - SMS 발송
        // - 카카오톡 알림
        // - 내부 메신저 알림
    }

    private void sendUrgentNotification(String message) {
        // 긴급 알림 발송
        log.error("긴급 알림 발송: {}", message);

        // 실제 구현 시:
        // - 관리자 휴대폰으로 SMS 발송
        // - Slack 긴급 채널로 메시지 발송
        // - 이메일 발송 (High Priority)
        // - 모니터링 시스템 알람
    }
}