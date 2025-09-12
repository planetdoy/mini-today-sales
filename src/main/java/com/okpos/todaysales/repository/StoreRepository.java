package com.okpos.todaysales.repository;

import com.okpos.todaysales.entity.Store;
import com.okpos.todaysales.entity.enums.StoreCategory;
import com.okpos.todaysales.entity.enums.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findByBusinessNumber(String businessNumber);
    
    List<Store> findByCategory(StoreCategory category);
    
    List<Store> findByStatus(StoreStatus status);
    
    List<Store> findByOwnerName(String ownerName);
    
    @Query("SELECT s FROM Store s WHERE s.storeName LIKE %:name%")
    List<Store> findByStoreNameContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Store s WHERE s.phoneNumber = :phoneNumber")
    Optional<Store> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}