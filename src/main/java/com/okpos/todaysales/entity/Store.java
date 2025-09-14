package com.okpos.todaysales.entity;

import com.okpos.todaysales.entity.enums.StoreCategory;
import com.okpos.todaysales.entity.enums.StoreStatus;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "sales")
@EqualsAndHashCode(of = "id")
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;
    
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;
    
    @Column(name = "owner_name", nullable = false, length = 50)
    private String ownerName;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(nullable = false, length = 200)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StoreCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StoreStatus status = StoreStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();
}