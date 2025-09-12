package com.okpos.todaysales.controller;

import com.okpos.todaysales.dto.*;
import com.okpos.todaysales.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Sales", description = "매출 관리 API")
public class SalesController {
    
    private final SalesService salesService;
    
    @Operation(
            summary = "매출 데이터 수신",
            description = "외부 POS 시스템이나 결제 시스템에서 매출 데이터를 전송받아 시스템에 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매출 등록 성공",
                    content = @Content(schema = @Schema(implementation = ServerApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "가맹점을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class)))
    })
    @PostMapping("/webhook")
    public ResponseEntity<ServerApiResponse<SaleResponse>> createSale(
            @Parameter(description = "매출 데이터", required = true)
            @Valid @RequestBody SaleRequest request) {
        try {
            log.info("매출 데이터 수신: {}", request.getOrderNumber());
            
            SaleResponse response = salesService.createSale(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ServerApiResponse.success("매출이 성공적으로 등록되었습니다", response));
                    
        } catch (IllegalArgumentException e) {
            log.error("매출 등록 실패 - 유효하지 않은 데이터: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            log.error("매출 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ServerApiResponse.error("매출 등록 중 오류가 발생했습니다"));
        }
    }
    
    @Operation(
            summary = "대시보드 조회",
            description = "특정 날짜의 매출 대시보드 데이터를 조회합니다. 일일 총매출, 거래건수, 결제수단별 통계, 시간대별 매출을 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대시보드 조회 성공",
                    content = @Content(schema = @Schema(implementation = ServerApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "가맹점을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class)))
    })
    @GetMapping("/dashboard/{businessNumber}")
    public ResponseEntity<ServerApiResponse<DashboardResponse>> getDashboard(
            @Parameter(description = "사업자번호 (xxx-xx-xxxxx 형식)", required = true, example = "123-45-67890")
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @Parameter(description = "조회 날짜 (기본값: 오늘)", example = "2024-01-15")
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
            
            return ResponseEntity.ok(ServerApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("대시보드 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ServerApiResponse.error("대시보드 조회 중 오류가 발생했습니다"));
        }
    }
    
    @Operation(
            summary = "매출 목록 조회",
            description = "지정된 기간의 매출 목록을 페이징 처리하여 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매출 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ServerApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "가맹점을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class)))
    })
    @GetMapping("/{businessNumber}")
    public ResponseEntity<ServerApiResponse<Page<SaleResponse>>> getSales(
            @Parameter(description = "사업자번호 (xxx-xx-xxxxx 형식)", required = true, example = "123-45-67890")
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @Parameter(description = "시작일시 (ISO DateTime 형식)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            @Parameter(description = "종료일시 (ISO DateTime 형식)", required = true, example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate,
            @Parameter(description = "페이징 정보 (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            log.info("매출 목록 조회: {} - {} to {}", businessNumber, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ServerApiResponse.error("시작일이 종료일보다 늦을 수 없습니다"));
            }
            
            Page<SaleResponse> sales = salesService.getSales(businessNumber, startDate, endDate, pageable);
            
            return ResponseEntity.ok(ServerApiResponse.success(sales));
            
        } catch (Exception e) {
            log.error("매출 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ServerApiResponse.error("매출 목록 조회 중 오류가 발생했습니다"));
        }
    }
    
    @Operation(
            summary = "월별 리포트 조회",
            description = "지정된 월의 상세한 매출 리포트를 조회합니다. 월별 총계, 일별 통계, 결제수단별 분석을 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "월별 리포트 조회 성공",
                    content = @Content(schema = @Schema(implementation = ServerApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "가맹점을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.okpos.todaysales.dto.ErrorResponse.class)))
    })
    @GetMapping("/report/monthly/{businessNumber}")
    public ResponseEntity<ServerApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @Parameter(description = "사업자번호 (xxx-xx-xxxxx 형식)", required = true, example = "123-45-67890")
            @PathVariable @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다") 
            String businessNumber,
            @Parameter(description = "조회할 년월 (YYYY-MM 형식)", required = true, example = "2024-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") 
            YearMonth yearMonth) {
        
        try {
            log.info("월별 리포트 조회: {} - {}", businessNumber, yearMonth);
            
            MonthlyReportResponse report = salesService.getMonthlyReport(businessNumber, yearMonth);
            
            return ResponseEntity.ok(ServerApiResponse.success(report));
            
        } catch (Exception e) {
            log.error("월별 리포트 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ServerApiResponse.error("월별 리포트 조회 중 오류가 발생했습니다"));
        }
    }
}