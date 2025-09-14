package com.okpos.todaysales.service;

import com.okpos.todaysales.dto.SettlementEvent;
import com.okpos.todaysales.entity.Settlement;
import com.okpos.todaysales.entity.enums.SettlementStatus;
import com.okpos.todaysales.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementFailureService {

    private final SettlementRepository settlementRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String SETTLEMENT_EXCHANGE = "sales.exchange";
    private static final String SETTLEMENT_ROUTING_KEY = "sales.settlement";
    private static final String NOTIFICATION_ROUTING_KEY = "sales.notification";

    /**
     * 정산 실패 상태를 별도 트랜잭션으로 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedSettlement(LocalDate settlementDate, String errorMessage) {
        try {
            log.info("정산 실패 기록 저장 시작: {}", settlementDate);

            // 해당 날짜의 정산 기록이 있는지 확인
            Settlement settlement = settlementRepository.findBySettlementDate(settlementDate)
                    .orElse(null);

            if (settlement != null) {
                // 기존 정산 기록을 실패로 변경
                settlement.setStatus(SettlementStatus.FAILED);
                settlement.setNote("Error: " + errorMessage);
                settlementRepository.save(settlement);
                log.info("기존 정산 기록을 실패로 변경: ID={}", settlement.getId());
            } else {
                // 정산 기록이 없으면 실패 기록 생성
                settlement = Settlement.builder()
                        .settlementDate(settlementDate)
                        .status(SettlementStatus.FAILED)
                        .note("Error: " + errorMessage)
                        .totalAmount(BigDecimal.ZERO)
                        .totalFee(BigDecimal.ZERO)
                        .netAmount(BigDecimal.ZERO)
                        .transactionCount(0)
                        .build();
                settlement = settlementRepository.save(settlement);
                log.info("새로운 실패 정산 기록 생성: ID={}", settlement.getId());
            }

            // 실패 이벤트 발행
            publishFailureEvent(settlement);

            // 긴급 알림 발송
            sendUrgentNotification(settlementDate, errorMessage);

        } catch (Exception e) {
            log.error("정산 실패 기록 저장 중 오류 발생: {}", settlementDate, e);
            // 실패 기록 저장도 실패한 경우, 최소한 알림은 발송
            sendCriticalAlert(settlementDate, errorMessage, e.getMessage());
        }
    }

    /**
     * 정산 실패 이벤트 발행
     */
    private void publishFailureEvent(Settlement settlement) {
        try {
            SettlementEvent event = SettlementEvent.builder()
                    .settlementId(settlement.getId())
                    .settlementDate(settlement.getSettlementDate())
                    .totalAmount(settlement.getTotalAmount())
                    .totalFee(settlement.getTotalFee())
                    .netAmount(settlement.getNetAmount())
                    .transactionCount(settlement.getTransactionCount())
                    .status("FAILED")
                    .message(settlement.getNote())
                    .processedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, SETTLEMENT_ROUTING_KEY, event);
            log.info("정산 실패 이벤트 발행 완료: {}", event);

        } catch (Exception e) {
            log.error("정산 실패 이벤트 발행 중 오류: {}", settlement.getId(), e);
        }
    }

    /**
     * 긴급 알림 발송
     */
    private void sendUrgentNotification(LocalDate settlementDate, String errorMessage) {
        try {
            String message = String.format(
                    "🚨 정산 실패 긴급 알림 🚨\n" +
                    "================================\n" +
                    "정산 날짜: %s\n" +
                    "오류 내용: %s\n" +
                    "발생 시간: %s\n" +
                    "================================\n" +
                    "즉시 관리자 확인이 필요합니다!",
                    settlementDate,
                    errorMessage,
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
            log.warn("긴급 알림 발송 완료: {}", settlementDate);

        } catch (Exception e) {
            log.error("긴급 알림 발송 실패: {}", settlementDate, e);
        }
    }

    /**
     * 치명적 오류 알림 (실패 기록 저장도 실패한 경우)
     */
    private void sendCriticalAlert(LocalDate settlementDate, String originalError, String saveError) {
        try {
            String message = String.format(
                    "💥 치명적 오류 발생 💥\n" +
                    "============================\n" +
                    "정산 날짜: %s\n" +
                    "원본 오류: %s\n" +
                    "저장 오류: %s\n" +
                    "발생 시간: %s\n" +
                    "============================\n" +
                    "시스템 점검이 필요합니다!",
                    settlementDate,
                    originalError,
                    saveError,
                    LocalDateTime.now()
            );

            // 알림 발송도 실패할 수 있으므로 로그에 기록
            log.error("CRITICAL ALERT: {}", message);
            rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);

        } catch (Exception e) {
            // 마지막 수단: 로그에만 기록
            log.error("치명적 오류 - 모든 알림 시스템 실패. 날짜: {}, 원본오류: {}, 저장오류: {}",
                    settlementDate, originalError, saveError, e);
        }
    }

    /**
     * 실패한 정산 재처리 전 검증
     */
    @Transactional(readOnly = true)
    public boolean canReprocess(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .map(settlement -> settlement.getStatus() == SettlementStatus.FAILED)
                .orElse(false);
    }

    /**
     * 실패 정산 목록 조회
     */
    @Transactional(readOnly = true)
    public Settlement getFailedSettlement(LocalDate settlementDate) {
        return settlementRepository.findBySettlementDate(settlementDate)
                .filter(settlement -> settlement.getStatus() == SettlementStatus.FAILED)
                .orElse(null);
    }
}