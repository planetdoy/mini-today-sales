package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import com.okpos.todaysales.entity.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    Optional<Sale> findByOrderNumber(String orderNumber);
    
    List<Sale> findByStoreId(Long storeId);
    
    List<Sale> findByPaymentType(PaymentType paymentType);
    
    List<Sale> findByChannel(SaleChannel channel);
    
    List<Sale> findByStatus(SaleStatus status);
    
    @Query("SELECT s FROM Sale s WHERE s.store.id = :storeId " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "ORDER BY s.transactionTime DESC")
    List<Sale> findByStoreIdAndDateRange(@Param("storeId") Long storeId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s " +
           "WHERE DATE(s.transactionTime) = :date " +
           "AND s.status = 'COMPLETED'")
    BigDecimal findTotalAmountByDate(@Param("date") LocalDate date);
    
    @Query("SELECT s FROM Sale s WHERE s.id NOT IN " +
           "(SELECT DISTINCT sale.id FROM Settlement settlement " +
           "JOIN settlement.store.sales sale " +
           "WHERE settlement.settlementDate = DATE(sale.transactionTime) " +
           "AND settlement.store = sale.store) " +
           "AND s.status = 'COMPLETED' " +
           "ORDER BY s.transactionTime DESC")
    List<Sale> findUnsettledSales();
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s " +
           "WHERE s.store.id = :storeId " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED'")
    BigDecimal findTotalAmountByStoreAndDateRange(@Param("storeId") Long storeId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.store.id = :storeId " +
           "AND DATE(s.transactionTime) = :date")
    Long countByStoreIdAndDate(@Param("storeId") Long storeId, 
                               @Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND DATE(s.transactionTime) = :date " +
           "AND s.status = 'COMPLETED'")
    BigDecimal findTotalAmountByBusinessNumberAndDate(@Param("businessNumber") String businessNumber,
                                                    @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(s) FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND DATE(s.transactionTime) = :date " +
           "AND s.status = 'COMPLETED'")
    Long countByBusinessNumberAndDate(@Param("businessNumber") String businessNumber,
                                    @Param("date") LocalDate date);
    
    @Query("SELECT s.paymentType as paymentType, " +
           "COALESCE(SUM(s.amount), 0) as amount, " +
           "COUNT(s) as count, " +
           "COALESCE(SUM(s.fee), 0) as fee, " +
           "COALESCE(SUM(s.netAmount), 0) as netAmount " +
           "FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND DATE(s.transactionTime) = :date " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY s.paymentType")
    List<Object[]> findPaymentTypeStatisticsByBusinessNumberAndDate(@Param("businessNumber") String businessNumber,
                                                                  @Param("date") LocalDate date);
    
    @Query("SELECT HOUR(s.transactionTime) as hour, " +
           "COALESCE(SUM(s.amount), 0) as amount, " +
           "COUNT(s) as count " +
           "FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND DATE(s.transactionTime) = :date " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY HOUR(s.transactionTime) " +
           "ORDER BY hour ")
    List<Object[]> findHourlyStatisticsByBusinessNumberAndDate(@Param("businessNumber") String businessNumber,
                                                             @Param("date") LocalDate date);
    
    @Query("SELECT s FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "ORDER BY s.transactionTime DESC")
    org.springframework.data.domain.Page<Sale> findByBusinessNumberAndDateRange(
            @Param("businessNumber") String businessNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(s.amount), 0), " +
           "COUNT(s), " +
           "COALESCE(SUM(s.fee), 0), " +
           "COALESCE(SUM(s.netAmount), 0) " +
           "FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED'")
    Object[] findMonthlyTotalByBusinessNumberAndDateRange(@Param("businessNumber") String businessNumber,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DAY(s.transactionTime) as day, " +
           "COALESCE(SUM(s.amount), 0) as amount, " +
           "COUNT(s) as count " +
           "FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY DAY(s.transactionTime) " +
           "ORDER BY day")
    List<Object[]> findDailyStatisticsByBusinessNumberAndDateRange(@Param("businessNumber") String businessNumber,
                                                                 @Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s.paymentType as paymentType, " +
           "COALESCE(SUM(s.amount), 0) as amount, " +
           "COUNT(s) as count, " +
           "COALESCE(SUM(s.fee), 0) as fee, " +
           "COALESCE(SUM(s.netAmount), 0) as netAmount " +
           "FROM Sale s " +
           "WHERE s.store.businessNumber = :businessNumber " +
           "AND s.transactionTime BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY s.paymentType")
    List<Object[]> findMonthlyPaymentTypeStatisticsByBusinessNumberAndDateRange(@Param("businessNumber") String businessNumber,
                                                                              @Param("startDate") LocalDateTime startDate,
                                                                              @Param("endDate") LocalDateTime endDate);
}