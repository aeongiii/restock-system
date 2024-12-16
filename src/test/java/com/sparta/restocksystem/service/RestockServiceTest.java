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
    private ProductRepository productRepository;

    @Mock
    private ProductNotificationHistoryRepository notificationHistoryRepository;

    @Mock
    private ProductUserNotificationRepository productUserNotificationRepository;

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
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(notificationHistoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.empty());
        when(productUserNotificationRepository.findByProductIdOrderByIdAsc(1L)).thenReturn(new ArrayList<>());
    // When
        restockService.sendNotification(1L); // 메서드 호출함
    // Then
        // productRepository.findByIdWithLock이 1번 호출되었는지 확인
        verify(productRepository, times(1)).findByIdWithLock(1L);
        // 재입고 회차가 2로 증가했는지 확인
        assertEquals(2L, product.getRestockRound());
    }

    @Test // 상품이 존재하지 않았을 때 예외가 발생하는지 확인
    void testSendNotification_ProductNotFound() {
    // Given
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

    // When & Then
        // restockService.sendNotification 호출 시 IllegalArgumentException이 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> restockService.sendNotification(1L));
        // 알림 히스토리가 저장되지 않았는지 확인
        verify(notificationHistoryRepository, never()).save(any());
    }
}
