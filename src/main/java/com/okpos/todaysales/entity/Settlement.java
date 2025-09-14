package com.okpos.todaysales.entity;

import com.okpos.todaysales.entity.enums.SettlementStatus;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlements", indexes = {
    @Index(name = "idx_settlement_date", columnList = "settlement_date"),
    @Index(name = "idx_settlement_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "sales")
@EqualsAndHashCode(of = "id")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "settlement_date", nullable = false, unique = true)
    private LocalDate settlementDate;
    
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "total_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFee = BigDecimal.ZERO;
    
    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void calculateNetAmount() {
        if (totalAmount != null && totalFee != null) {
            this.netAmount = totalAmount.subtract(totalFee);
        }
    }
}