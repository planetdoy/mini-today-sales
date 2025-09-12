package com.okpos.todaysales.dto;

import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import com.okpos.todaysales.entity.enums.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "매출 응답 데이터")
public class SaleResponse {
    
    @Schema(description = "매출 ID", example = "1")
    private Long id;
    
    @Schema(description = "사업자번호", example = "123-45-67890")
    private String businessNumber;
    
    @Schema(description = "가맹점명", example = "테스트 매장")
    private String storeName;
    
    @Schema(description = "거래시간", example = "2024-01-15T14:30:00")
    private LocalDateTime transactionTime;
    
    @Schema(description = "결제금액", example = "25000")
    private BigDecimal amount;
    
    @Schema(description = "결제수단", example = "CARD")
    private PaymentType paymentType;
    
    @Schema(description = "판매채널", example = "ONLINE")
    private SaleChannel channel;
    
    @Schema(description = "주문번호", example = "ORDER-20240115-001")
    private String orderNumber;
    
    @Schema(description = "수수료", example = "625.00")
    private BigDecimal fee;
    
    @Schema(description = "순매출", example = "24375.00")
    private BigDecimal netAmount;
    
    @Schema(description = "매출 상태", example = "COMPLETED")
    private SaleStatus status;
}