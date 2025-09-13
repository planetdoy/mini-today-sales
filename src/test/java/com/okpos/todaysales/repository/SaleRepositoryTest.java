package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SaleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SaleRepository saleRepository;

    private Store store1;
    private Store store2;
    private Sale sale1;
    private Sale sale2;
    private Sale sale3;
    private Sale sale4;

    @BeforeEach
    void setUp() {
        store1 = Store.builder()
                .businessNumber("123-45-67890")
                .storeName("카페 스타벅스")
                .ownerName("김영희")
                .phoneNumber("02-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .category(StoreCategory.CAFE)
                .status(StoreStatus.ACTIVE)
                .build();

        store2 = Store.builder()
                .businessNumber("987-65-43210")
                .storeName("레스토랑 맘스터치")
                .ownerName("박철수")
                .phoneNumber("02-9876-5432")
                .address("서울시 서초구 반포대로 456")
                .category(StoreCategory.RESTAURANT)
                .status(StoreStatus.ACTIVE)
                .build();

        entityManager.persistAndFlush(store1);
        entityManager.persistAndFlush(store2);

        LocalDateTime today = LocalDateTime.of(2023, 12, 1, 10, 30);
        LocalDateTime yesterday = LocalDateTime.of(2023, 11, 30, 14, 20);

        sale1 = Sale.builder()
                .store(store1)
                .transactionTime(today)
                .amount(new BigDecimal("15000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORD-001")
                .fee(new BigDecimal("300"))
                .netAmount(new BigDecimal("14700"))
                .status(SaleStatus.COMPLETED)
                .build();

        sale2 = Sale.builder()
                .store(store1)
                .transactionTime(yesterday)
                .amount(new BigDecimal("25000"))
                .paymentType(PaymentType.CASH)
                .channel(SaleChannel.OFFLINE)
                .orderNumber("ORD-002")
                .fee(BigDecimal.ZERO)
                .netAmount(new BigDecimal("25000"))
                .status(SaleStatus.COMPLETED)
                .build();

        sale3 = Sale.builder()
                .store(store2)
                .transactionTime(today)
                .amount(new BigDecimal("35000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORD-003")
                .fee(new BigDecimal("700"))
                .netAmount(new BigDecimal("34300"))
                .status(SaleStatus.COMPLETED)
                .build();

        sale4 = Sale.builder()
                .store(store1)
                .transactionTime(today.minusHours(2))
                .amount(new BigDecimal("8000"))
                .paymentType(PaymentType.MOBILE_PAY)
                .channel(SaleChannel.MOBILE_APP)
                .orderNumber("ORD-004")
                .fee(new BigDecimal("160"))
                .netAmount(new BigDecimal("7840"))
                .status(SaleStatus.CANCELLED)
                .build();

        entityManager.persistAndFlush(sale1);
        entityManager.persistAndFlush(sale2);
        entityManager.persistAndFlush(sale3);
        entityManager.persistAndFlush(sale4);
    }

    @Test
    void findByOrderNumber_ShouldReturnSale_WhenExists() {
        Optional<Sale> result = saleRepository.findByOrderNumber("ORD-001");
        
        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualTo(new BigDecimal("15000"));
    }

    @Test
    void findByStoreId_ShouldReturnSalesForStore() {
        List<Sale> result = saleRepository.findByStoreId(store1.getId());
        
        assertThat(result).hasSize(3);
    }

    @Test
    void findByPaymentType_ShouldReturnSalesWithPaymentType() {
        List<Sale> result = saleRepository.findByPaymentType(PaymentType.CARD);
        
        assertThat(result).hasSize(2);
    }

    @Test
    void findByChannel_ShouldReturnSalesWithChannel() {
        List<Sale> result = saleRepository.findByChannel(SaleChannel.ONLINE);
        
        assertThat(result).hasSize(2);
    }

    @Test
    void findByStatus_ShouldReturnSalesWithStatus() {
        List<Sale> completedSales = saleRepository.findByStatus(SaleStatus.COMPLETED);
        List<Sale> cancelledSales = saleRepository.findByStatus(SaleStatus.CANCELLED);
        
        assertThat(completedSales).hasSize(3);
        assertThat(cancelledSales).hasSize(1);
    }

    @Test
    void findByStoreIdAndDateRange_ShouldReturnSalesInRange() {
        LocalDateTime startDate = LocalDateTime.of(2023, 12, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 12, 1, 23, 59);
        
        List<Sale> result = saleRepository.findByStoreIdAndDateRange(
                store1.getId(), startDate, endDate);
        
        assertThat(result).hasSize(2);
    }

    @Test
    void findTotalAmountByDate_ShouldReturnCorrectSum() {
        LocalDate date = LocalDate.of(2023, 12, 1);
        
        BigDecimal result = saleRepository.findTotalAmountByDate(date);
        
        assertThat(result).isEqualTo(new BigDecimal("50000"));
    }

    @Test
    void findTotalAmountByStoreAndDateRange_ShouldReturnCorrectSum() {
        LocalDateTime startDate = LocalDateTime.of(2023, 12, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 12, 1, 23, 59);
        
        BigDecimal result = saleRepository.findTotalAmountByStoreAndDateRange(
                store1.getId(), startDate, endDate);
        
        assertThat(result).isEqualTo(new BigDecimal("15000"));
    }

    @Test
    void countByStoreIdAndDate_ShouldReturnCorrectCount() {
        LocalDate date = LocalDate.of(2023, 12, 1);
        
        Long count = saleRepository.countByStoreIdAndDate(store1.getId(), date);
        
        assertThat(count).isEqualTo(2L);
    }
}