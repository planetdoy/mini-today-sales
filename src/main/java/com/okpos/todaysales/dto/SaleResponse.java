package com.okpos.todaysales.dto;

import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import com.okpos.todaysales.entity.enums.SaleStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {
    
    private Long id;
    private String businessNumber;
    private String storeName;
    private LocalDateTime transactionTime;
    private BigDecimal amount;
    private PaymentType paymentType;
    private SaleChannel channel;
    private String orderNumber;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private SaleStatus status;
}