package com.sparta.restocksystem.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter  // 상품
public class Product {

    // PK : 상품 아이디 -> FK로 사용됨
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 재입고 회차 : 외래키로는 안했지만 다 동일하게 맞춰줘야 한다.
    @Column(name = "restock_round", nullable = false)
    private Long restockRound;

    // 재고 상태
    @Column(name = "stock_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    // 재고 상태는 둘 중 하나이다.
    public enum StockStatus {
        IN_STOCK, // 기본값
        OUT_OF_STOCK
    }

}
