package com.okpos.todaysales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "대시보드 응답 데이터")
public class DashboardResponse {
    
    @Schema(description = "조회 날짜", example = "2024-01-15")
    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;
    
    @Schema(description = "일일 총 매출", example = "75000")
    @NotNull(message = "총 매출은 필수입니다")
    @PositiveOrZero(message = "총 매출은 0 이상이어야 합니다")
    private BigDecimal totalAmount;
    
    @Schema(description = "일일 총 거래건수", example = "5")
    @NotNull(message = "총 거래건수는 필수입니다")
    @PositiveOrZero(message = "총 거래건수는 0 이상이어야 합니다")
    private Integer totalCount;
    
    @Schema(description = "결제수단별 통계")
    @Valid
    private List<PaymentTypeStatistic> paymentTypeStatistics;
    
    @Schema(description = "시간대별 통계")
    @Valid
    private List<HourlyStatistic> hourlyStatistics;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentTypeStatistic {
        
        @NotNull(message = "결제수단은 필수입니다")
        private String paymentType;
        
        @NotNull(message = "결제수단별 매출은 필수입니다")
        @PositiveOrZero(message = "결제수단별 매출은 0 이상이어야 합니다")
        private BigDecimal amount;
        
        @NotNull(message = "결제수단별 거래건수는 필수입니다")
        @PositiveOrZero(message = "결제수단별 거래건수는 0 이상이어야 합니다")
        private Integer count;
        
        @NotNull(message = "수수료는 필수입니다")
        @PositiveOrZero(message = "수수료는 0 이상이어야 합니다")
        private BigDecimal fee;
        
        @NotNull(message = "순매출은 필수입니다")
        @PositiveOrZero(message = "순매출은 0 이상이어야 합니다")
        private BigDecimal netAmount;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyStatistic {
        
        @NotNull(message = "시간은 필수입니다")
        private Integer hour;
        
        @NotNull(message = "시간별 매출은 필수입니다")
        @PositiveOrZero(message = "시간별 매출은 0 이상이어야 합니다")
        private BigDecimal amount;
        
        @NotNull(message = "시간별 거래건수는 필수입니다")
        @PositiveOrZero(message = "시간별 거래건수는 0 이상이어야 합니다")
        private Integer count;
    }
}