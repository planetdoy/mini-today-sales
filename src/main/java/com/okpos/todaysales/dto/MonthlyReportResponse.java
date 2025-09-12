package com.okpos.todaysales.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportResponse {
    
    @NotNull(message = "년월은 필수입니다")
    private YearMonth yearMonth;
    
    @NotNull(message = "월 총매출은 필수입니다")
    @PositiveOrZero(message = "월 총매출은 0 이상이어야 합니다")
    private BigDecimal totalAmount;
    
    @NotNull(message = "월 총거래건수는 필수입니다")
    @PositiveOrZero(message = "월 총거래건수는 0 이상이어야 합니다")
    private Integer totalCount;
    
    @NotNull(message = "월 총수수료는 필수입니다")
    @PositiveOrZero(message = "월 총수수료는 0 이상이어야 합니다")
    private BigDecimal totalFee;
    
    @NotNull(message = "월 순매출은 필수입니다")
    @PositiveOrZero(message = "월 순매출은 0 이상이어야 합니다")
    private BigDecimal totalNetAmount;
    
    @Valid
    private List<DailyStatistic> dailyStatistics;
    
    @Valid
    private List<PaymentTypeStatistic> paymentTypeStatistics;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStatistic {
        
        @NotNull(message = "일자는 필수입니다")
        private Integer day;
        
        @NotNull(message = "일별 매출은 필수입니다")
        @PositiveOrZero(message = "일별 매출은 0 이상이어야 합니다")
        private BigDecimal amount;
        
        @NotNull(message = "일별 거래건수는 필수입니다")
        @PositiveOrZero(message = "일별 거래건수는 0 이상이어야 합니다")
        private Integer count;
    }
    
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
}