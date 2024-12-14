package com.sparta.restocksystem.repository;

import com.sparta.restocksystem.entity.ProductNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductNotificationHistoryRepository extends JpaRepository<ProductNotificationHistory, Long> {

    Optional<Object> findByProductId(Long productId);
}
