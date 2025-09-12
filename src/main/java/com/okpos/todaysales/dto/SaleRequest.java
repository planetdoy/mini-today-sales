package com.okpos.todaysales.dto;

import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequest {
    
    @NotNull(message = "사업자번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다")
    private String businessNumber;
    
    @NotNull(message = "거래시간은 필수입니다")
    private LocalDateTime transactionTime;
    
    @NotNull(message = "결제금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "결제금액은 0보다 커야 합니다")
    private BigDecimal amount;
    
    @NotNull(message = "결제수단은 필수입니다")
    private PaymentType paymentType;
    
    @NotNull(message = "판매채널은 필수입니다")
    private SaleChannel channel;
    
    @NotBlank(message = "주문번호는 필수입니다")
    @Size(max = 50, message = "주문번호는 50자를 초과할 수 없습니다")
    private String orderNumber;
}