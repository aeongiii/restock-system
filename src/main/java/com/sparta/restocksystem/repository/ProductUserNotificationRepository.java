package com.sparta.restocksystem.repository;

import com.sparta.restocksystem.entity.ProductUserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUserNotificationRepository extends JpaRepository<ProductUserNotification, Long> {
}
