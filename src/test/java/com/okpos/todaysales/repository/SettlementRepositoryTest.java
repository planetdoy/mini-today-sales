package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Settlement;
import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.SettlementStatus;
import com.okpos.todaysales.entity.enums.StoreCategory;
import com.okpos.todaysales.entity.enums.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SettlementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SettlementRepository settlementRepository;

    private Store store1;
    private Store store2;
    private Settlement settlement1;
    private Settlement settlement2;
    private Settlement settlement3;
    private Settlement settlement4;

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

        LocalDate today = LocalDate.of(2023, 12, 1);
        LocalDate yesterday = LocalDate.of(2023, 11, 30);
        LocalDate lastWeek = LocalDate.of(2023, 11, 24);

        settlement1 = Settlement.builder()
                .store(store1)
                .settlementDate(today)
                .totalAmount(new BigDecimal("100000"))
                .totalFee(new BigDecimal("2000"))
                .settlementAmount(new BigDecimal("98000"))
                .status(SettlementStatus.COMPLETED)
                .build();

        settlement2 = Settlement.builder()
                .store(store1)
                .settlementDate(yesterday)
                .totalAmount(new BigDecimal("150000"))
                .totalFee(new BigDecimal("3000"))
                .settlementAmount(new BigDecimal("147000"))
                .status(SettlementStatus.PENDING)
                .build();

        settlement3 = Settlement.builder()
                .store(store2)
                .settlementDate(today)
                .totalAmount(new BigDecimal("200000"))
                .totalFee(new BigDecimal("4000"))
                .settlementAmount(new BigDecimal("196000"))
                .status(SettlementStatus.COMPLETED)
                .build();

        settlement4 = Settlement.builder()
                .store(store1)
                .settlementDate(lastWeek)
                .totalAmount(new BigDecimal("80000"))
                .totalFee(new BigDecimal("1600"))
                .settlementAmount(new BigDecimal("78400"))
                .status(SettlementStatus.PENDING)
                .build();

        entityManager.persistAndFlush(settlement1);
        entityManager.persistAndFlush(settlement2);
        entityManager.persistAndFlush(settlement3);
        entityManager.persistAndFlush(settlement4);
    }

    @Test
    void findByStoreId_ShouldReturnSettlementsForStore() {
        List<Settlement> result = settlementRepository.findByStoreId(store1.getId());
        
        assertThat(result).hasSize(3);
    }

    @Test
    void findByStatus_ShouldReturnSettlementsWithStatus() {
        List<Settlement> completedSettlements = settlementRepository.findByStatus(SettlementStatus.COMPLETED);
        List<Settlement> pendingSettlements = settlementRepository.findByStatus(SettlementStatus.PENDING);
        
        assertThat(completedSettlements).hasSize(2);
        assertThat(pendingSettlements).hasSize(2);
    }

    @Test
    void findBySettlementDate_ShouldReturnSettlementsForDate() {
        LocalDate today = LocalDate.of(2023, 12, 1);
        
        List<Settlement> result = settlementRepository.findBySettlementDate(today);
        
        assertThat(result).hasSize(2);
    }

    @Test
    void findByStoreIdAndSettlementDate_ShouldReturnSpecificSettlement() {
        LocalDate today = LocalDate.of(2023, 12, 1);
        
        Optional<Settlement> result = settlementRepository.findByStoreIdAndSettlementDate(
                store1.getId(), today);
        
        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualTo(new BigDecimal("100000"));
    }

    @Test
    void findByStoreIdAndDateRange_ShouldReturnSettlementsInRange() {
        LocalDate startDate = LocalDate.of(2023, 11, 30);
        LocalDate endDate = LocalDate.of(2023, 12, 1);
        
        List<Settlement> result = settlementRepository.findByStoreIdAndDateRange(
                store1.getId(), startDate, endDate);
        
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Settlement::getSettlementDate)
                .containsExactly(LocalDate.of(2023, 12, 1), LocalDate.of(2023, 11, 30));
    }

    @Test
    void findTotalSettlementAmountByStoreAndDateRange_ShouldReturnCorrectSum() {
        LocalDate startDate = LocalDate.of(2023, 11, 30);
        LocalDate endDate = LocalDate.of(2023, 12, 1);
        
        BigDecimal result = settlementRepository.findTotalSettlementAmountByStoreAndDateRange(
                store1.getId(), startDate, endDate);
        
        assertThat(result).isEqualTo(new BigDecimal("98000"));
    }

    @Test
    void findPendingSettlementsBeforeDate_ShouldReturnPendingSettlements() {
        LocalDate currentDate = LocalDate.of(2023, 12, 1);
        
        List<Settlement> result = settlementRepository.findPendingSettlementsBeforeDate(currentDate);
        
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Settlement::getSettlementDate)
                .containsExactly(LocalDate.of(2023, 11, 24), LocalDate.of(2023, 11, 30));
    }
}