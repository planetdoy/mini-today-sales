package com.okpos.todaysales.entity;

import com.okpos.todaysales.entity.enums.SettlementStatus;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "settlements", indexes = {
    @Index(name = "idx_settlement_store_id", columnList = "store_id"),
    @Index(name = "idx_settlement_date", columnList = "settlement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "store")
@EqualsAndHashCode(of = "id")
public class Settlement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;
    
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "total_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFee = BigDecimal.ZERO;
    
    @Column(name = "settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;
    
    @PrePersist
    @PreUpdate
    public void calculateSettlementAmount() {
        if (totalAmount != null && totalFee != null) {
            this.settlementAmount = totalAmount.subtract(totalFee);
        }
    }
}