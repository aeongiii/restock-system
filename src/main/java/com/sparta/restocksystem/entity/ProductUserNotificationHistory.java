package com.sparta.restocksystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product_user_notification_history")
@NoArgsConstructor  // 상품 + 유저별 알림 히스토리
public class ProductUserNotificationHistory {

    // PK : 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK : 상품 아이디
    @ManyToOne
    @JoinColumn (name = "product_id", nullable = false)
    private Product product;

    // FK : 유저 아이디
    @ManyToOne
    @JoinColumn (name = "user_id", nullable = false)
    private ProductUserNotification productUserNotification;

    // 재입고 회차
    @Column(name = "restock_round", nullable = false)
    private Long restockRound;

    // 발송 날짜
    @Column(name = "notification_date", nullable = false)
    private LocalDateTime notificationDate;
}
