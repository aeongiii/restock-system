package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.entity.ProductUserNotification;
import com.sparta.restocksystem.repository.ProductNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserNotificationServiceTest {

    @InjectMocks
    private UserNotificationService userNotificationService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductNotificationHistoryRepository productNotificationHistoryRepository;

    @Mock
    private ProductUserNotificationRepository productUserNotificationRepository;

    @Mock
    private ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;

    @Test // 초당 500개 저장 + 모든 유저에게 알림 발송 되는지
    void testSendNotificationToUser_RateLimiter() {
        MockitoAnnotations.openMocks(this);

        // Given
        Product product = new Product();
        product.setId(1L);

        ProductNotificationHistory notificationHistory = new ProductNotificationHistory();

        // 1000개 유저 데이터 만들기
        List<ProductUserNotification> userList = new ArrayList<>();
        for (long i = 1; i <= 1000; i++) {
            ProductUserNotification notification = new ProductUserNotification();
            notification.setId(i);
            userList.add(notification);
        }

        // 재고가 없는 경우 false 반환
        when(productService.isOutOfStock(product)).thenReturn(false);

        // When
        long startTime = System.currentTimeMillis();
        userNotificationService.sendNotificationsToUsers(product, notificationHistory, userList);
        long endTime = System.currentTimeMillis();

        // Then
        long elapsedTime = endTime - startTime; // 소요시간
        assertTrue(elapsedTime >= 2000, "최소 2초 이상 걸려야 한다");

        // isOutOfStock() 1000번 호출되어야 한다.
        verify(productService, times(1000)).isOutOfStock(product);
    }
}