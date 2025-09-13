package com.okpos.todaysales.service;

import com.okpos.todaysales.dto.MonthlyReportResponse;
import com.okpos.todaysales.dto.SaleDashboard;
import com.okpos.todaysales.dto.SaleRequest;
import com.okpos.todaysales.dto.SaleResponse;
import com.okpos.todaysales.event.EventPublisher;
import com.okpos.todaysales.exception.InvalidRequestException;
import com.okpos.todaysales.exception.StoreNotFoundException;
import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleStatus;
import com.okpos.todaysales.repository.SaleRepository;
import com.okpos.todaysales.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {
    
    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;
    private final EventPublisher eventPublisher;
    
    private static final BigDecimal CARD_FEE_RATE = new BigDecimal("0.025"); // 2.5%
    private static final BigDecimal CASH_FEE_RATE = BigDecimal.ZERO; // 0%
    
    @Transactional
    @CacheEvict(value = "dashboard", key = "#request.businessNumber + '_' + T(java.time.LocalDate).now()")
    public SaleResponse createSale(SaleRequest request) {
        // 중복 주문번호 검증
        if (saleRepository.findByOrderNumber(request.getOrderNumber()).isPresent()) {
            throw new InvalidRequestException("orderNumber", request.getOrderNumber(), 
                    "이미 존재하는 주문번호입니다: " + request.getOrderNumber());
        }
        
        // 거래시간 검증 (미래 시간 불가)
        if (request.getTransactionTime().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new InvalidRequestException("transactionTime", request.getTransactionTime(), 
                    "거래시간이 현재시간보다 늦을 수 없습니다");
        }
        
        Store store = storeRepository.findByBusinessNumber(request.getBusinessNumber())
                .orElseThrow(() -> new StoreNotFoundException(request.getBusinessNumber()));
        
        // 가맹점 상태 검증
        if (store.getStatus() != com.okpos.todaysales.entity.enums.StoreStatus.ACTIVE) {
            throw new InvalidRequestException("businessNumber", request.getBusinessNumber(), 
                    "비활성화된 가맹점입니다");
        }
        
        // 수수료 계산
        BigDecimal fee = calculateFee(request.getAmount(), request.getPaymentType());
        
        // 순수익 계산
        BigDecimal netAmount = request.getAmount().subtract(fee);
        
        Sale sale = Sale.builder()
                .store(store)
                .transactionTime(request.getTransactionTime())
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .channel(request.getChannel())
                .orderNumber(request.getOrderNumber())
                .fee(fee)
                .netAmount(netAmount)
                .status(SaleStatus.COMPLETED)
                .build();
        
        Sale savedSale = saleRepository.save(sale);
        
        try {
            eventPublisher.publishSaleCreated(savedSale);
        } catch (Exception e) {
            // 이벤트 발행 실패는 비즈니스 로직에 영향을 주지 않도록 로그만 남김
            throw e; // 하지만 트랜잭션 롤백을 위해 다시 던짐
        }
        
        return convertToSaleResponse(savedSale);
    }
    
    @Cacheable(value = "dashboard", key = "#businessNumber + '_' + #date")
    public SaleDashboard getDashboard(String businessNumber, LocalDate date) {
        // 가맹점 존재 여부 검증
        Store store = storeRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new StoreNotFoundException(businessNumber));
        
        // 조회 날짜 검증 (미래 날짜 불가)
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidRequestException("date", date, "미래 날짜는 조회할 수 없습니다");
        }
        
        // 일일 총 매출 및 거래 건수
        BigDecimal totalAmount = saleRepository.findTotalAmountByBusinessNumberAndDate(businessNumber, date);
        Long totalCount = saleRepository.countByBusinessNumberAndDate(businessNumber, date);
        
        // 결제수단별 통계
        List<Object[]> paymentStats = saleRepository.findPaymentTypeStatisticsByBusinessNumberAndDate(businessNumber, date);
        List<SaleDashboard.PaymentTypeStatistic> paymentTypeStatistics = paymentStats.stream()
                .map(stat -> SaleDashboard.PaymentTypeStatistic.builder()
                        .paymentType(((PaymentType) stat[0]).getDescription())
                        .amount((BigDecimal) stat[1])
                        .count(((Long) stat[2]).intValue())
                        .fee((BigDecimal) stat[3])
                        .netAmount((BigDecimal) stat[4])
                        .build())
                .collect(Collectors.toList());
        
        // 시간대별 매출
        List<Object[]> hourlyStats = saleRepository.findHourlyStatisticsByBusinessNumberAndDate(businessNumber, date);
        List<SaleDashboard.HourlyStatistic> hourlyStatistics = hourlyStats.stream()
                .map(stat -> SaleDashboard.HourlyStatistic.builder()
                        .hour((Integer) stat[0])
                        .amount((BigDecimal) stat[1])
                        .count(((Long) stat[2]).intValue())
                        .build())
                .collect(Collectors.toList());
        
        return SaleDashboard.builder()
                .date(date)
                .totalAmount(totalAmount)
                .totalCount(totalCount.intValue())
                .paymentTypeStatistics(paymentTypeStatistics)
                .hourlyStatistics(hourlyStatistics)
                .build();
    }
    
    public Page<SaleResponse> getSales(String businessNumber, LocalDateTime startDate, 
                                     LocalDateTime endDate, Pageable pageable) {
        // 가맹점 존재 여부 검증
        storeRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new StoreNotFoundException(businessNumber));
        
        // 날짜 범위 검증
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("dateRange", 
                    String.format("%s ~ %s", startDate, endDate), 
                    "시작일이 종료일보다 늦을 수 없습니다");
        }
        
        // 조회 기간 제한 (최대 1년)
        if (startDate.isBefore(endDate.minusYears(1))) {
            throw new InvalidRequestException("dateRange", 
                    String.format("%s ~ %s", startDate, endDate), 
                    "조회 기간이 1년을 초과할 수 없습니다");
        }
        
        Page<Sale> salesPage = saleRepository.findByBusinessNumberAndDateRange(
                businessNumber, startDate, endDate, pageable);
        
        return salesPage.map(this::convertToSaleResponse);
    }
    
    public MonthlyReportResponse getMonthlyReport(String businessNumber, YearMonth yearMonth) {
        // 가맹점 존재 여부 검증
        storeRepository.findByBusinessNumber(businessNumber)
                .orElseThrow(() -> new StoreNotFoundException(businessNumber));
        
        // 조회 월 검증 (미래 월 불가)
        if (yearMonth.isAfter(YearMonth.now())) {
            throw new InvalidRequestException("yearMonth", yearMonth, "미래 월은 조회할 수 없습니다");
        }
        
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // 월별 총계 조회
        Object[] monthlyTotal = saleRepository.findMonthlyTotalByBusinessNumberAndDateRange(
                businessNumber, startDate, endDate);
        
        BigDecimal totalAmount = (BigDecimal) monthlyTotal[0];
        Long totalCount = (Long) monthlyTotal[1];
        BigDecimal totalFee = (BigDecimal) monthlyTotal[2];
        BigDecimal totalNetAmount = (BigDecimal) monthlyTotal[3];
        
        // 일별 통계
        List<Object[]> dailyStats = saleRepository.findDailyStatisticsByBusinessNumberAndDateRange(
                businessNumber, startDate, endDate);
        List<MonthlyReportResponse.DailyStatistic> dailyStatistics = dailyStats.stream()
                .map(stat -> MonthlyReportResponse.DailyStatistic.builder()
                        .day((Integer) stat[0])
                        .amount((BigDecimal) stat[1])
                        .count(((Long) stat[2]).intValue())
                        .build())
                .collect(Collectors.toList());
        
        // 결제수단별 통계
        List<Object[]> paymentStats = saleRepository.findMonthlyPaymentTypeStatisticsByBusinessNumberAndDateRange(
                businessNumber, startDate, endDate);
        List<MonthlyReportResponse.PaymentTypeStatistic> paymentTypeStatistics = paymentStats.stream()
                .map(stat -> MonthlyReportResponse.PaymentTypeStatistic.builder()
                        .paymentType(((PaymentType) stat[0]).getDescription())
                        .amount((BigDecimal) stat[1])
                        .count(((Long) stat[2]).intValue())
                        .fee((BigDecimal) stat[3])
                        .netAmount((BigDecimal) stat[4])
                        .build())
                .collect(Collectors.toList());
        
        return MonthlyReportResponse.builder()
                .yearMonth(yearMonth)
                .totalAmount(totalAmount)
                .totalCount(totalCount.intValue())
                .totalFee(totalFee)
                .totalNetAmount(totalNetAmount)
                .dailyStatistics(dailyStatistics)
                .paymentTypeStatistics(paymentTypeStatistics)
                .build();
    }
    
    private BigDecimal calculateFee(BigDecimal amount, PaymentType paymentType) {
        BigDecimal feeRate = paymentType == PaymentType.CARD ? CARD_FEE_RATE : CASH_FEE_RATE;
        return amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    private SaleResponse convertToSaleResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .businessNumber(sale.getStore().getBusinessNumber())
                .storeName(sale.getStore().getStoreName())
                .transactionTime(sale.getTransactionTime())
                .amount(sale.getAmount())
                .paymentType(sale.getPaymentType())
                .channel(sale.getChannel())
                .orderNumber(sale.getOrderNumber())
                .fee(sale.getFee())
                .netAmount(sale.getNetAmount())
                .status(sale.getStatus())
                .build();
    }
}