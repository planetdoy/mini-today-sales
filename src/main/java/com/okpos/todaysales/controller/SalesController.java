package com.okpos.todaysales.controller;

import com.okpos.todaysales.dto.*;
import com.okpos.todaysales.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Validated
public class SalesController {
    
    private final SalesService salesService;
    
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(@Valid @RequestBody SaleRequest request) {
        try {
            log.info("매출 데이터 수신: {}", request.getOrderNumber());
            
            SaleResponse response = salesService.createSale(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("매출이 성공적으로 등록되었습니다", response));
                    
        } catch (IllegalArgumentException e) {
            log.error("매출 등록 실패 - 유효하지 않은 데이터: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            log.error("매출 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("매출 등록 중 오류가 발생했습니다"));
        }
    }
    
    @GetMapping("/dashboard/{businessNumber}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate date) {
        
        try {
            log.info("대시보드 조회: {} - {}", businessNumber, date);
            
            SaleDashboard dashboard = salesService.getDashboard(businessNumber, date);
            
            // SaleDashboard를 DashboardResponse로 변환
            DashboardResponse response = DashboardResponse.builder()
                    .date(dashboard.getDate())
                    .totalAmount(dashboard.getTotalAmount())
                    .totalCount(dashboard.getTotalCount())
                    .paymentTypeStatistics(dashboard.getPaymentTypeStatistics().stream()
                            .map(stat -> DashboardResponse.PaymentTypeStatistic.builder()
                                    .paymentType(stat.getPaymentType())
                                    .amount(stat.getAmount())
                                    .count(stat.getCount())
                                    .fee(stat.getFee())
                                    .netAmount(stat.getNetAmount())
                                    .build())
                            .collect(java.util.stream.Collectors.toList()))
                    .hourlyStatistics(dashboard.getHourlyStatistics().stream()
                            .map(stat -> DashboardResponse.HourlyStatistic.builder()
                                    .hour(stat.getHour())
                                    .amount(stat.getAmount())
                                    .count(stat.getCount())
                                    .build())
                            .collect(java.util.stream.Collectors.toList()))
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("대시보드 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("대시보드 조회 중 오류가 발생했습니다"));
        }
    }
    
    @GetMapping("/{businessNumber}")
    public ResponseEntity<ApiResponse<Page<SaleResponse>>> getSales(
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            log.info("매출 목록 조회: {} - {} to {}", businessNumber, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("시작일이 종료일보다 늦을 수 없습니다"));
            }
            
            Page<SaleResponse> sales = salesService.getSales(businessNumber, startDate, endDate, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(sales));
            
        } catch (Exception e) {
            log.error("매출 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("매출 목록 조회 중 오류가 발생했습니다"));
        }
    }
    
    @GetMapping("/report/monthly/{businessNumber}")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") 
            YearMonth yearMonth) {
        
        try {
            log.info("월별 리포트 조회: {} - {}", businessNumber, yearMonth);
            
            MonthlyReportResponse report = salesService.getMonthlyReport(businessNumber, yearMonth);
            
            return ResponseEntity.ok(ApiResponse.success(report));
            
        } catch (Exception e) {
            log.error("월별 리포트 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("월별 리포트 조회 중 오류가 발생했습니다"));
        }
    }
}