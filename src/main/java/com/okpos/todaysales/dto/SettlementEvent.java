package com.okpos.todaysales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEvent implements Serializable {

    private Long settlementId;
    private LocalDate settlementDate;
    private BigDecimal totalAmount;
    private BigDecimal totalFee;
    private BigDecimal netAmount;
    private Integer transactionCount;
    private String status;
    private LocalDateTime processedAt;
    private String message;
}