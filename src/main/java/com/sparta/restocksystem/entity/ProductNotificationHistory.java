package com.sparta.restocksystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_notification_history")
@NoArgsConstructor
@Getter
@Setter  // 상품별 재입고 알림 히스토리
public class ProductNotificationHistory {

    // PK : id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK : 상품 아이디
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 재입고 회차
    @Column(name = "restock_round", nullable = false)
    private Long restockRound;

    // 재입고 알림 발송 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "restock_notification_status", nullable = false)
    private RestockNotificationStatus restockNotificationStatus;

    // 마지막 발송 유저 아이디
    // 기본 -1로 저장하고 그 이후로 발송 시 제대로 저장하기?
    @Column(name = "last_notification_user_Id", nullable = false)
    private Long lastNotificationUserId;

    public enum RestockNotificationStatus {
        IN_PROGRESS, CANCELED_BY_SOLD_OUT, CANCELED_BY_ERROR, COMPLETED
    }
}
