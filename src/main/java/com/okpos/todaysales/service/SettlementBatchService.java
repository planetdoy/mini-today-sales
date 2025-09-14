package com.okpos.todaysales.service;

import com.okpos.todaysales.dto.SettlementEvent;
import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.Settlement;
import com.okpos.todaysales.entity.enums.SettlementStatus;
import com.okpos.todaysales.repository.SaleRepository;
import com.okpos.todaysales.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementBatchService {

    private final SaleRepository saleRepository;
    private final SettlementRepository settlementRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SettlementFailureService settlementFailureService;
    private final MetricsService metricsService;

    private static final BigDecimal FEE_RATE = new BigDecimal("0.03"); // 3% 수수료율
    private static final String SETTLEMENT_EXCHANGE = "sales.exchange";
    private static final String SETTLEMENT_ROUTING_KEY = "sales.settlement";
    private static final String NOTIFICATION_ROUTING_KEY = "sales.notification";

    /**
     * 매일 새벽 2시에 전일 매출 정산 실행
     * fixedDelay 추가로 중복 실행 방지
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void runDailySettlement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("=== 일일 정산 배치 시작: {} ===", yesterday);

        try {
            // 중복 실행 방지 체크
            if (settlementRepository.existsBySettlementDate(yesterday)) {
                log.warn("이미 정산된 날짜입니다. 스킵: {}", yesterday);
                return;
            }

            Settlement result = processSettlement(yesterday);
            log.info("=== 일일 정산 배치 완료: {} | 정산금액: {}원 ===",
                    yesterday, result.getNetAmount());

        } catch (Exception e) {
            log.error("=== 일일 정산 배치 실패: {} ===", yesterday, e);
            sendFailureNotification(yesterday, e.getMessage());
        }
    }

    /**
     * 수동 정산 실행
     */
    @Transactional
    public Settlement processManualSettlement(LocalDate settlementDate) {
        log.info("수동 정산 시작: {}", settlementDate);
        return processSettlement(settlementDate);
    }

    /**
     * 정산 프로세스 메인 로직
     */
    public Settlement processSettlement(LocalDate settlementDate) {
        io.micrometer.core.instrument.Timer.Sample sample = metricsService.startSettlementTimer();

        try {
            Settlement result = processSettlementInTransaction(settlementDate);

            // 성공 메트릭 기록
            metricsService.recordSettlementCompleted(sample, result.getTransactionCount(), result.getTotalAmount());

            return result;
        } catch (Exception e) {
            log.error("정산 처리 중 오류 발생: {}", settlementDate, e);

            // 실패 메트릭 기록
            metricsService.recordSettlementFailed(sample, e.getClass().getSimpleName(), e.getMessage());

            // 별도 서비스로 실패 처리
            settlementFailureService.saveFailedSettlement(settlementDate, e.getMessage());

            throw new RuntimeException("Settlement processing failed for date: " + settlementDate, e);
        }
    }

    /**
     * 트랜잭션 내에서 실행되는 정산 로직
     */
    @Transactional
    public Settlement processSettlementInTransaction(LocalDate settlementDate) {
        // 이미 정산된 날짜인지 확인
        if (settlementRepository.existsBySettlementDate(settlementDate)) {
            log.warn("이미 정산된 날짜입니다: {}", settlementDate);
            throw new IllegalStateException("Settlement already exists for date: " + settlementDate);
        }

        // 정산 엔티티 생성
        Settlement settlement = Settlement.builder()
                .settlementDate(settlementDate)
                .status(SettlementStatus.PROCESSING)
                .totalAmount(BigDecimal.ZERO)
                .totalFee(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO)
                .transactionCount(0)
                .build();

        settlement = settlementRepository.save(settlement);

        // 전일 미정산 매출 조회
            LocalDateTime startOfDay = settlementDate.atStartOfDay();
            LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);
            List<Sale> unsettledSales = saleRepository.findUnsettledSalesByDateRange(startOfDay, endOfDay);

            if (unsettledSales.isEmpty()) {
                log.info("정산할 매출이 없습니다: {}", settlementDate);
                settlement.setStatus(SettlementStatus.COMPLETED);
                settlement.setNote("No sales to settle");
                settlement.setCompletedAt(LocalDateTime.now());
                return settlementRepository.save(settlement);
            }

            // 정산 계산
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalFee = BigDecimal.ZERO;
            int transactionCount = 0;

            for (Sale sale : unsettledSales) {
                // 수수료 계산
                BigDecimal fee = calculateFee(sale.getAmount(), sale.getPaymentType().name());
                sale.setFee(fee);
                sale.setNetAmount(sale.getAmount().subtract(fee));
                sale.setSettlement(settlement);
                sale.setIsSettled(true);

                // 합계 계산
                totalAmount = totalAmount.add(sale.getAmount());
                totalFee = totalFee.add(fee);
                transactionCount++;
            }

            // 정산 정보 업데이트
            settlement.setTotalAmount(totalAmount);
            settlement.setTotalFee(totalFee);
            settlement.setNetAmount(totalAmount.subtract(totalFee));
            settlement.setTransactionCount(transactionCount);
            settlement.setStatus(SettlementStatus.COMPLETED);
            settlement.setCompletedAt(LocalDateTime.now());
            settlement.getSales().addAll(unsettledSales);

            // 저장
            saleRepository.saveAll(unsettledSales);
            settlement = settlementRepository.save(settlement);

            log.info("정산 완료 - 날짜: {}, 거래수: {}, 총액: {}, 수수료: {}, 순액: {}",
                    settlementDate, transactionCount, totalAmount, totalFee, settlement.getNetAmount());

            // 정산 완료 이벤트 발행
            publishSettlementEvent(settlement, "COMPLETED");

            return settlement;
    }


    /**
     * 수수료 계산
     */
    private BigDecimal calculateFee(BigDecimal amount, String paymentType) {
        BigDecimal rate = FEE_RATE;

        // 결제 유형별 수수료율 차등 적용
        switch (paymentType) {
            case "CARD":
                rate = new BigDecimal("0.025"); // 2.5%
                break;
            case "CASH":
                rate = BigDecimal.ZERO; // 0%
                break;
            case "TRANSFER":
                rate = new BigDecimal("0.01"); // 1%
                break;
            case "POINT":
                rate = new BigDecimal("0.02"); // 2%
                break;
            default:
                rate = FEE_RATE; // 기본 3%
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 정산 이벤트 발행
     */
    private void publishSettlementEvent(Settlement settlement, String status) {
        SettlementEvent event = SettlementEvent.builder()
                .settlementId(settlement.getId())
                .settlementDate(settlement.getSettlementDate())
                .totalAmount(settlement.getTotalAmount())
                .totalFee(settlement.getTotalFee())
                .netAmount(settlement.getNetAmount())
                .transactionCount(settlement.getTransactionCount())
                .status(status)
                .processedAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, SETTLEMENT_ROUTING_KEY, event);
        log.info("정산 이벤트 발행: {}", event);
    }

    /**
     * 실패 알림 발송
     */
    private void sendFailureNotification(LocalDate settlementDate, String errorMessage) {
        String message = String.format(
                "정산 실패 알림\n날짜: %s\n오류: %s\n시간: %s",
                settlementDate, errorMessage, LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
        log.error("정산 실패 알림 발송: {}", message);
    }

    /**
     * 미정산 매출 조회
     */
    public List<Sale> getUnsettledSales(LocalDate date) {
        return saleRepository.findUnsettledSalesByDate(date);
    }

    /**
     * 재정산 처리
     */
    @Transactional
    public Settlement reprocessSettlement(Long settlementId) {
        // 재처리 가능 여부 확인
        if (!settlementFailureService.canReprocess(settlementId)) {
            throw new IllegalStateException("Settlement cannot be reprocessed: " + settlementId);
        }

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));

        // 기존 정산 삭제
        LocalDate settlementDate = settlement.getSettlementDate();
        settlementRepository.delete(settlement);

        // 재정산 실행
        return processSettlement(settlementDate);
    }
}