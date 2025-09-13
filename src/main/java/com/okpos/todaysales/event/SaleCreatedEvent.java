package com.okpos.todaysales.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreatedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;
    
    private Long saleId;
    private Long storeId;
    private String storeName;
    private String posNumber;
    private String receiptNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String saleStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDate;
    
    private String createdBy;
    private LocalDateTime createdAt;
    
    private String correlationId;
    private Integer version;
    
    public static SaleCreatedEvent from(Long saleId, Long storeId, String storeName, 
                                       String posNumber, String receiptNumber, 
                                       BigDecimal amount, String paymentMethod, 
                                       String saleStatus, LocalDateTime saleDate, 
                                       String createdBy) {
        return SaleCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("SALE_CREATED")
                .eventTimestamp(LocalDateTime.now())
                .saleId(saleId)
                .storeId(storeId)
                .storeName(storeName)
                .posNumber(posNumber)
                .receiptNumber(receiptNumber)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .saleStatus(saleStatus)
                .saleDate(saleDate)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .version(1)
                .build();
    }
}