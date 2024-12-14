package com.sparta.restocksystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Product_user_notification")
@NoArgsConstructor  // 상품별 재입고 알림을 설정한 유저
public class ProductUserNotification {

    // PK : 유저 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK : 상품 아이디
    @ManyToOne
    @JoinColumn (name = "product_id", nullable = false)
    private Product product;

    // 활성화 여부
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // 생성 날짜
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist // 생성 날짜 자동 저장
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 수정 날짜
    @Column(name = "updated_at", nullable = true) // null 가능
    private LocalDateTime updatedAt;

    @PreUpdate // 엔티티 값 변경 시 수정날짜 찍힘
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
