package com.okpos.todaysales.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDashboard {
    
    private LocalDate date;
    private BigDecimal totalAmount;
    private Integer totalCount;
    private List<PaymentTypeStatistic> paymentTypeStatistics;
    private List<HourlyStatistic> hourlyStatistics;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentTypeStatistic {
        private String paymentType;
        private BigDecimal amount;
        private Integer count;
        private BigDecimal fee;
        private BigDecimal netAmount;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyStatistic {
        private Integer hour;
        private BigDecimal amount;
        private Integer count;
    }
}