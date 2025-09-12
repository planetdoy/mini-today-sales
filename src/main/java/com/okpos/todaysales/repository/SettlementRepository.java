package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Settlement;
import com.okpos.todaysales.entity.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    
    List<Settlement> findByStoreId(Long storeId);
    
    List<Settlement> findByStatus(SettlementStatus status);
    
    List<Settlement> findBySettlementDate(LocalDate settlementDate);
    
    Optional<Settlement> findByStoreIdAndSettlementDate(Long storeId, LocalDate settlementDate);
    
    @Query("SELECT s FROM Settlement s WHERE s.store.id = :storeId " +
           "AND s.settlementDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.settlementDate DESC")
    List<Settlement> findByStoreIdAndDateRange(@Param("storeId") Long storeId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(s.settlementAmount), 0) FROM Settlement s " +
           "WHERE s.store.id = :storeId " +
           "AND s.settlementDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED'")
    BigDecimal findTotalSettlementAmountByStoreAndDateRange(@Param("storeId") Long storeId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Settlement s WHERE s.status = 'PENDING' " +
           "AND s.settlementDate <= :date " +
           "ORDER BY s.settlementDate ASC")
    List<Settlement> findPendingSettlementsBeforeDate(@Param("date") LocalDate date);
}