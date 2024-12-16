package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.repository.ProductNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RestockServiceTest {

    @InjectMocks // RestockService 객체 생성 + @Mock 객체를 주입
    private RestockService restockService;

    @Mock
    private ProductService productService;

    @Mock
    private NotificationHistoryService notificationHistoryService;

    @Mock
    private ProductUserNotificationRepository productUserNotificationRepository;

    @Mock
    private UserNotificationService userNotificationService;

    private Product product;

    @BeforeEach
    void setUp() {
        // 애너테이션이 붙은 객체들을 초기화한다.
        MockitoAnnotations.openMocks(this);

        // 기본 Product 객체 설정
        product = new Product();
        product.setId(1L);
        product.setRestockRound(1L);
        product.setStockStatus(Product.StockStatus.IN_STOCK);
    }

    @Test
    void testSendNotification_Success() {

        // Given
        when(productService.updateProductRestockRound(1L)).thenAnswer(invocation -> {
            product.setRestockRound(product.getRestockRound() + 1);
            return product;
        });
        when(notificationHistoryService.updateNotificationHistory(product, 1L))
                .thenReturn(new ProductNotificationHistory());
        when(userNotificationService.fetchUserList(1L))
                .thenReturn(new ArrayList<>());

        // when
        restockService.sendNotification(1L);

        // Then
        verify(productService, times(1)).updateProductRestockRound(1L);
        assertEquals(2L, product.getRestockRound());

    }

    @Test // 상품이 존재하지 않았을 때 예외가 발생하는지 확인
    void testSendNotification_ProductNotFound() {
        // Given
        when(productService.updateProductRestockRound(1L))
                .thenThrow(new IllegalArgumentException("상품이 존재하지 않습니다."));

        // When & Then
        // restockService.sendNotification 호출 시 IllegalArgumentException이 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> restockService.sendNotification(1L));
        // 알림 히스토리가 저장되지 않았는지 확인
        verify(notificationHistoryService, never()).updateNotificationHistory(any(), anyLong());
    }
}
