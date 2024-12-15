package com.sparta.restocksystem.repository;

import com.sparta.restocksystem.entity.ProductNotificationHistory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory, Long> {

    Optional<Object> findByProductId(Long productId);

    // 비관적 락 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM ProductNotificationHistory h WHERE h.product.id = :productId")
    Optional<ProductNotificationHistory> findByProductIdWithLock(@Param("productId") Long productId);
}
