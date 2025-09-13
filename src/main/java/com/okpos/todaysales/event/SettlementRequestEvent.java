package com.okpos.todaysales.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRequestEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;
    
    private Long storeId;
    private String storeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settlementDate;
    
    private BigDecimal totalAmount;
    private Long transactionCount;
    
    private Map<String, BigDecimal> paymentMethodBreakdown;
    private Map<String, Long> paymentMethodCounts;
    
    private String requestedBy;
    private LocalDateTime requestedAt;
    
    private String settlementStatus;
    private String settlementPeriod;
    
    private String correlationId;
    private Integer version;
    
    public static SettlementRequestEvent create(Long storeId, String storeName, 
                                               LocalDate settlementDate, 
                                               BigDecimal totalAmount, 
                                               Long transactionCount,
                                               Map<String, BigDecimal> paymentBreakdown,
                                               Map<String, Long> paymentCounts,
                                               String requestedBy) {
        return SettlementRequestEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("SETTLEMENT_REQUEST")
                .eventTimestamp(LocalDateTime.now())
                .storeId(storeId)
                .storeName(storeName)
                .settlementDate(settlementDate)
                .totalAmount(totalAmount)
                .transactionCount(transactionCount)
                .paymentMethodBreakdown(paymentBreakdown)
                .paymentMethodCounts(paymentCounts)
                .requestedBy(requestedBy)
                .requestedAt(LocalDateTime.now())
                .settlementStatus("PENDING")
                .settlementPeriod("DAILY")
                .correlationId(UUID.randomUUID().toString())
                .version(1)
                .build();
    }
    
    public static SettlementRequestEvent simpleCreate(Long storeId, LocalDate date) {
        return SettlementRequestEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("SETTLEMENT_REQUEST")
                .eventTimestamp(LocalDateTime.now())
                .storeId(storeId)
                .settlementDate(date)
                .requestedAt(LocalDateTime.now())
                .settlementStatus("PENDING")
                .settlementPeriod("DAILY")
                .correlationId(UUID.randomUUID().toString())
                .version(1)
                .build();
    }
}