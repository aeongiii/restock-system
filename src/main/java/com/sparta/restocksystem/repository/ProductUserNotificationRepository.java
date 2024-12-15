package com.sparta.restocksystem.repository;

import com.sparta.restocksystem.entity.ProductUserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductUserNotificationRepository extends JpaRepository<ProductUserNotification, Long> {
    // 유저 리스트 가져오기
    List<ProductUserNotification> findByProductIdOrderByIdAsc(Long productId);

    // lastuserid 이후 유저부터 리스트 가져오기
    List<ProductUserNotification> findByProductIdAndIdGreaterThanOrderByIdAsc(Long productId, Long lastUserId);
}
