package com.okpos.todaysales.service;

import com.okpos.todaysales.dto.SaleDashboard;
import com.okpos.todaysales.dto.SaleRequest;
import com.okpos.todaysales.dto.SaleResponse;
import com.okpos.todaysales.entity.Sale;
import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.*;
import com.okpos.todaysales.repository.SaleRepository;
import com.okpos.todaysales.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {
    
    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private StoreRepository storeRepository;
    
    @InjectMocks
    private SalesService salesService;
    
    private Store testStore;
    private SaleRequest cardSaleRequest;
    private SaleRequest cashSaleRequest;
    
    @BeforeEach
    void setUp() {
        testStore = Store.builder()
                .id(1L)
                .businessNumber("123-45-67890")
                .storeName("테스트 매장")
                .ownerName("김사장")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .category(StoreCategory.RESTAURANT)
                .status(StoreStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        cardSaleRequest = SaleRequest.builder()
                .businessNumber("123-45-67890")
                .transactionTime(LocalDateTime.now())
                .amount(new BigDecimal("10000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-001")
                .build();
        
        cashSaleRequest = SaleRequest.builder()
                .businessNumber("123-45-67890")
                .transactionTime(LocalDateTime.now())
                .amount(new BigDecimal("5000"))
                .paymentType(PaymentType.CASH)
                .channel(SaleChannel.OFFLINE)
                .orderNumber("ORDER-002")
                .build();
    }
    
    @Test
    @DisplayName("카드 결제 매출 생성 - 수수료 2.5% 적용")
    void createCardSale() {
        // given
        when(storeRepository.findByBusinessNumber("123-45-67890"))
                .thenReturn(Optional.of(testStore));
        
        Sale expectedSale = Sale.builder()
                .id(1L)
                .store(testStore)
                .transactionTime(cardSaleRequest.getTransactionTime())
                .amount(new BigDecimal("10000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-001")
                .fee(new BigDecimal("250.00")) // 2.5%
                .netAmount(new BigDecimal("9750.00"))
                .status(SaleStatus.COMPLETED)
                .build();
        
        when(saleRepository.save(any(Sale.class))).thenReturn(expectedSale);
        
        // when
        SaleResponse response = salesService.createSale(cardSaleRequest);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("10000"));
        assertThat(response.getFee()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.getNetAmount()).isEqualTo(new BigDecimal("9750.00"));
        assertThat(response.getPaymentType()).isEqualTo(PaymentType.CARD);
        
        verify(storeRepository).findByBusinessNumber("123-45-67890");
        verify(saleRepository).save(any(Sale.class));
    }
    
    @Test
    @DisplayName("현금 결제 매출 생성 - 수수료 0% 적용")
    void createCashSale() {
        // given
        when(storeRepository.findByBusinessNumber("123-45-67890"))
                .thenReturn(Optional.of(testStore));
        
        Sale expectedSale = Sale.builder()
                .id(2L)
                .store(testStore)
                .transactionTime(cashSaleRequest.getTransactionTime())
                .amount(new BigDecimal("5000"))
                .paymentType(PaymentType.CASH)
                .channel(SaleChannel.OFFLINE)
                .orderNumber("ORDER-002")
                .fee(BigDecimal.ZERO)
                .netAmount(new BigDecimal("5000"))
                .status(SaleStatus.COMPLETED)
                .build();
        
        when(saleRepository.save(any(Sale.class))).thenReturn(expectedSale);
        
        // when
        SaleResponse response = salesService.createSale(cashSaleRequest);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("5000"));
        assertThat(response.getFee()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getNetAmount()).isEqualTo(new BigDecimal("5000"));
        assertThat(response.getPaymentType()).isEqualTo(PaymentType.CASH);
    }
    
    @Test
    @DisplayName("존재하지 않는 사업자번호로 매출 생성시 예외 발생")
    void createSaleWithInvalidBusinessNumber() {
        // given
        when(storeRepository.findByBusinessNumber("999-99-99999"))
                .thenReturn(Optional.empty());
        
        SaleRequest invalidRequest = SaleRequest.builder()
                .businessNumber("999-99-99999")
                .transactionTime(LocalDateTime.now())
                .amount(new BigDecimal("10000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-003")
                .build();
        
        // when & then
        assertThatThrownBy(() -> salesService.createSale(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가맹점을 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("대시보드 데이터 조회")
    void getDashboard() {
        // given
        LocalDate testDate = LocalDate.now();
        String businessNumber = "123-45-67890";
        
        when(saleRepository.findTotalAmountByBusinessNumberAndDate(businessNumber, testDate))
                .thenReturn(new BigDecimal("50000"));
        when(saleRepository.countByBusinessNumberAndDate(businessNumber, testDate))
                .thenReturn(5L);
        
        // 결제수단별 통계 모킹
        List<Object[]> paymentStats = Arrays.asList(
                new Object[]{PaymentType.CARD, new BigDecimal("30000"), 3L, new BigDecimal("750"), new BigDecimal("29250")},
                new Object[]{PaymentType.CASH, new BigDecimal("20000"), 2L, BigDecimal.ZERO, new BigDecimal("20000")}
        );
        when(saleRepository.findPaymentTypeStatisticsByBusinessNumberAndDate(businessNumber, testDate))
                .thenReturn(paymentStats);
        
        // 시간대별 통계 모킹
        List<Object[]> hourlyStats = Arrays.asList(
                new Object[]{9, new BigDecimal("10000"), 1L},
                new Object[]{14, new BigDecimal("25000"), 2L},
                new Object[]{19, new BigDecimal("15000"), 2L}
        );
        when(saleRepository.findHourlyStatisticsByBusinessNumberAndDate(businessNumber, testDate))
                .thenReturn(hourlyStats);
        
        // when
        SaleDashboard dashboard = salesService.getDashboard(businessNumber, testDate);
        
        // then
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getDate()).isEqualTo(testDate);
        assertThat(dashboard.getTotalAmount()).isEqualTo(new BigDecimal("50000"));
        assertThat(dashboard.getTotalCount()).isEqualTo(5);
        assertThat(dashboard.getPaymentTypeStatistics()).hasSize(2);
        assertThat(dashboard.getHourlyStatistics()).hasSize(3);
    }
    
    @Test
    @DisplayName("매출 목록 조회 - 페이징")
    void getSalesWithPaging() {
        // given
        String businessNumber = "123-45-67890";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        Sale sale1 = Sale.builder()
                .id(1L)
                .store(testStore)
                .transactionTime(LocalDateTime.now().minusDays(1))
                .amount(new BigDecimal("10000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-001")
                .fee(new BigDecimal("250"))
                .netAmount(new BigDecimal("9750"))
                .status(SaleStatus.COMPLETED)
                .build();
        
        Sale sale2 = Sale.builder()
                .id(2L)
                .store(testStore)
                .transactionTime(LocalDateTime.now().minusDays(2))
                .amount(new BigDecimal("5000"))
                .paymentType(PaymentType.CASH)
                .channel(SaleChannel.OFFLINE)
                .orderNumber("ORDER-002")
                .fee(BigDecimal.ZERO)
                .netAmount(new BigDecimal("5000"))
                .status(SaleStatus.COMPLETED)
                .build();
        
        List<Sale> sales = Arrays.asList(sale1, sale2);
        Page<Sale> salesPage = new PageImpl<>(sales, pageable, 2);
        
        when(saleRepository.findByBusinessNumberAndDateRange(businessNumber, startDate, endDate, pageable))
                .thenReturn(salesPage);
        
        // when
        Page<SaleResponse> result = salesService.getSales(businessNumber, startDate, endDate, pageable);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getOrderNumber()).isEqualTo("ORDER-001");
        assertThat(result.getContent().get(1).getOrderNumber()).isEqualTo("ORDER-002");
    }
}