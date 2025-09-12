package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.StoreCategory;
import com.okpos.todaysales.entity.enums.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class StoreRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StoreRepository storeRepository;

    private Store store1;
    private Store store2;
    private Store store3;

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

        store3 = Store.builder()
                .businessNumber("555-44-33222")
                .storeName("편의점 세븐일레븐")
                .ownerName("이민수")
                .phoneNumber("02-5555-4444")
                .address("서울시 종로구 세종로 789")
                .category(StoreCategory.CONVENIENCE)
                .status(StoreStatus.INACTIVE)
                .build();

        entityManager.persistAndFlush(store1);
        entityManager.persistAndFlush(store2);
        entityManager.persistAndFlush(store3);
    }

    @Test
    void findByBusinessNumber_ShouldReturnStore_WhenExists() {
        Optional<Store> result = storeRepository.findByBusinessNumber("123-45-67890");
        
        assertThat(result).isPresent();
        assertThat(result.get().getStoreName()).isEqualTo("카페 스타벅스");
    }

    @Test
    void findByBusinessNumber_ShouldReturnEmpty_WhenNotExists() {
        Optional<Store> result = storeRepository.findByBusinessNumber("999-99-99999");
        
        assertThat(result).isEmpty();
    }

    @Test
    void findByCategory_ShouldReturnStoresWithCategory() {
        List<Store> result = storeRepository.findByCategory(StoreCategory.CAFE);
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreName()).isEqualTo("카페 스타벅스");
    }

    @Test
    void findByStatus_ShouldReturnStoresWithStatus() {
        List<Store> activeStores = storeRepository.findByStatus(StoreStatus.ACTIVE);
        List<Store> inactiveStores = storeRepository.findByStatus(StoreStatus.INACTIVE);
        
        assertThat(activeStores).hasSize(2);
        assertThat(inactiveStores).hasSize(1);
        assertThat(inactiveStores.get(0).getStoreName()).isEqualTo("편의점 세븐일레븐");
    }

    @Test
    void findByOwnerName_ShouldReturnStoresWithOwnerName() {
        List<Store> result = storeRepository.findByOwnerName("김영희");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreName()).isEqualTo("카페 스타벅스");
    }

    @Test
    void findByStoreNameContaining_ShouldReturnStoresContainingName() {
        List<Store> result = storeRepository.findByStoreNameContaining("카페");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreName()).isEqualTo("카페 스타벅스");
    }

    @Test
    void findByPhoneNumber_ShouldReturnStore_WhenExists() {
        Optional<Store> result = storeRepository.findByPhoneNumber("02-1234-5678");
        
        assertThat(result).isPresent();
        assertThat(result.get().getStoreName()).isEqualTo("카페 스타벅스");
    }

    @Test
    void findByPhoneNumber_ShouldReturnEmpty_WhenNotExists() {
        Optional<Store> result = storeRepository.findByPhoneNumber("02-9999-9999");
        
        assertThat(result).isEmpty();
    }
}