package com.okpos.todaysales.dto;

import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "매출 등록 요청 데이터")
public class SaleRequest {
    
    @Schema(description = "사업자번호", example = "123-45-67890", pattern = "^\\d{3}-\\d{2}-\\d{5}$")
    @NotNull(message = "사업자번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다")
    private String businessNumber;
    
    @Schema(description = "거래시간", example = "2024-01-15T14:30:00")
    @NotNull(message = "거래시간은 필수입니다")
    private LocalDateTime transactionTime;
    
    @Schema(description = "결제금액", example = "25000", minimum = "0.01")
    @NotNull(message = "결제금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "결제금액은 0보다 커야 합니다")
    private BigDecimal amount;
    
    @Schema(description = "결제수단", example = "CARD", allowableValues = {"CARD", "CASH"})
    @NotNull(message = "결제수단은 필수입니다")
    private PaymentType paymentType;
    
    @Schema(description = "판매채널", example = "ONLINE", allowableValues = {"ONLINE", "OFFLINE"})
    @NotNull(message = "판매채널은 필수입니다")
    private SaleChannel channel;
    
    @Schema(description = "주문번호", example = "ORDER-20240115-001", maxLength = 50)
    @NotBlank(message = "주문번호는 필수입니다")
    @Size(max = 50, message = "주문번호는 50자를 초과할 수 없습니다")
    private String orderNumber;
}