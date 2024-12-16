package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.repository.ProductNotificationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationHistoryService {

    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductService productService;

    public NotificationHistoryService(ProductNotificationHistoryRepository productNotificationHistoryRepository, ProductService productService) {
        this.productNotificationHistoryRepository = productNotificationHistoryRepository;
        this.productService = productService;
    }

    // 재입고 알림 발송 상태 IN_PROGRESS 로 변경
    @Transactional
    public ProductNotificationHistory updateNotificationHistory(Product product, Long productId) {
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository.findByProductIdWithLock(productId)
                .orElseGet(() -> {
                    ProductNotificationHistory newHistory = new ProductNotificationHistory();
                    newHistory.setProduct(product);
                    return newHistory;
                });
        notificationHistory.setProduct(product);
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.IN_PROGRESS);
        notificationHistory.setRestockRound(product.getRestockRound());
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("재입고 알림 발송 상태 IN_PROGRESS로 변경 완료");
        return notificationHistory;
    }

    // 알림히스토리 저장
    @Transactional
    public void saveNotificationHistory(ProductNotificationHistory notificationHistory) {
        productNotificationHistoryRepository.save(notificationHistory);
    }

    // 기존 발송 정보 확인
    @Transactional(readOnly = true)
    public ProductNotificationHistory getNotificationHistoryWithLock(Long productId) {
        return productNotificationHistoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품에 대한 기존 발송 정보가 없습니다. productId = " + productId));
    }

    // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
    public void completeNotification(ProductNotificationHistory notificationHistory, Product product) {
        if (!productService.isOutOfStock(product)) {
            notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.COMPLETED);
            productNotificationHistoryRepository.save(notificationHistory);
            System.out.println("알림 발송이 성공적으로 완료되었습니다.");
        }
    }
}
