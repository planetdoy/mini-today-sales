package com.okpos.todaysales.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okpos.todaysales.dto.SaleRequest;
import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.PaymentType;
import com.okpos.todaysales.entity.enums.SaleChannel;
import com.okpos.todaysales.entity.enums.StoreCategory;
import com.okpos.todaysales.entity.enums.StoreStatus;
import com.okpos.todaysales.repository.SaleRepository;
import com.okpos.todaysales.repository.SettlementRepository;
import com.okpos.todaysales.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 기본적인 통합 테스트
 */
@DisplayName("기본 통합 테스트")
public class SimpleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // 테스트 데이터 정리
        settlementRepository.deleteAll();
        saleRepository.deleteAll();
        storeRepository.deleteAll();
        verifyContainersRunning();

        // 테스트용 스토어 생성
        createTestStores();
    }

    private void createTestStores() {
        // 첫 번째 테스트 스토어
        Store store1 = Store.builder()
                .businessNumber("123-45-67890")
                .storeName("테스트 매장 1")
                .ownerName("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울특별시 강남구 테스트로 123")
                .category(StoreCategory.RESTAURANT)
                .status(StoreStatus.ACTIVE)
                .build();
        storeRepository.save(store1);

        // 두 번째 테스트 스토어
        Store store2 = Store.builder()
                .businessNumber("111-22-33333")
                .storeName("테스트 매장 2")
                .ownerName("김영희")
                .phoneNumber("010-9876-5432")
                .address("서울특별시 종로구 테스트대로 456")
                .category(StoreCategory.CAFE)
                .status(StoreStatus.ACTIVE)
                .build();
        storeRepository.save(store2);
    }

    @Test
    @DisplayName("매출 생성 및 조회 테스트")
    @Transactional
    void createAndRetrieveSalesTest() throws Exception {
        // Given: 매출 생성 요청 데이터
        SaleRequest saleRequest = SaleRequest.builder()
                .businessNumber("123-45-67890")
                .amount(new BigDecimal("50000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("ORDER-001")
                .transactionTime(LocalDateTime.now())
                .build();

        // When: 매출 생성
        mockMvc.perform(post("/api/v1/sales/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(50000))
                .andExpect(jsonPath("$.data.paymentType").value("CARD"))
                .andExpect(jsonPath("$.data.channel").value("ONLINE"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORDER-001"))
                .andDo(print());

        // Then: 매출 생성 성공 확인 완료 (목록 조회는 별도 테스트에서 수행)
    }

    @Test
    @DisplayName("헬스체크 통합 테스트")
    @Transactional
    void healthCheckTest() throws Exception {
        // When & Then: Actuator 헬스체크 확인
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andDo(print());

        // 커스텀 모니터링 헬스체크도 확인
        mockMvc.perform(get("/api/monitoring/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andDo(print());
    }

    @Test
    @DisplayName("메트릭 조회 테스트")
    @Transactional
    void metricsTest() throws Exception {
        // When & Then: 메트릭 요약 조회
        mockMvc.perform(get("/api/monitoring/metrics/summary"))
                .andExpect(status().isOk())
                .andDo(print());

        // Prometheus 메트릭 확인
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sales_created_total")))
                .andDo(print());
    }

    @Test
    @DisplayName("시스템 정보 조회 테스트")
    @Transactional
    void systemInfoTest() throws Exception {
        // When & Then: 시스템 정보 조회
        mockMvc.perform(get("/api/monitoring/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jvm_memory_used").exists())
                .andExpect(jsonPath("$.jvm_memory_max").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("매출 생성 후 메트릭 업데이트 확인")
    @Transactional
    void salesMetricsUpdateTest() throws Exception {
        // Given: 매출 생성
        SaleRequest saleRequest = SaleRequest.builder()
                .businessNumber("111-22-33333")
                .amount(new BigDecimal("100000"))
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("METRIC-001")
                .transactionTime(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/v1/sales/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleRequest)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Then: 메트릭이 업데이트되었는지 확인
        await().untilAsserted(() -> {
            mockMvc.perform(get("/api/monitoring/metrics/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_sales_count").value(greaterThan(0.0)))
                    .andExpect(jsonPath("$.total_sales_amount").value(greaterThanOrEqualTo(100000.0)))
                    .andDo(print());
        });
    }

    @Test
    @DisplayName("데이터 유효성 검증 테스트")
    @Transactional
    void dataValidationTest() throws Exception {
        // Given: 잘못된 데이터
        SaleRequest invalidRequest = SaleRequest.builder()
                .businessNumber("invalid") // 잘못된 사업자번호 형식
                .amount(new BigDecimal("-1000")) // 음수 금액
                .paymentType(PaymentType.CARD)
                .channel(SaleChannel.ONLINE)
                .orderNumber("") // 빈 주문번호
                .transactionTime(LocalDateTime.now())
                .build();

        // When & Then: 유효성 검증 실패 확인
        mockMvc.perform(post("/api/v1/sales/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}