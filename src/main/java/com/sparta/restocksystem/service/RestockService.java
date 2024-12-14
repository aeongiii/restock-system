package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.entity.ProductUserNotification;
import com.sparta.restocksystem.entity.ProductUserNotificationHistory;
import com.sparta.restocksystem.repository.ProductNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestockService {

    private final ProductRepository productRepository;
    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;

    public RestockService(ProductRepository productRepository,
                          ProductUserNotificationRepository productUserNotificationRepository,
                          ProductNotificationHistoryRepository productNotificationHistoryRepository,
                          ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository) {
        this.productRepository = productRepository;
        this.productUserNotificationRepository = productUserNotificationRepository;
        this.productNotificationHistoryRepository = productNotificationHistoryRepository;
        this.productUserNotificationHistoryRepository = productUserNotificationHistoryRepository;
    }

    // 알림 발송
    public void sendNotification(Long productId) {

        // Product 수정 : 재입고 회차 +1
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId : " + productId));
        Long nextRestockRound = (product.getRestockRound() + 1);
        product.setRestockRound(nextRestockRound);
        product.setStockStatus(Product.StockStatus.IN_STOCK);
        productRepository.save(product);
        System.out.println("재입고 회차 +1 완료 !");

        // ProductNotificationHistory 수정 : 재입고 알림 발송 상태 IN_PROGRESS 으로 변경
        ProductNotificationHistory notificationHistory = (ProductNotificationHistory) productNotificationHistoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    // 없으면 자동으로 만들기
                    ProductNotificationHistory newHistory = new ProductNotificationHistory();
                    newHistory.setProduct(product);
                    return newHistory;
                });
        notificationHistory.setProduct(product);
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.IN_PROGRESS);
        notificationHistory.setRestockRound(nextRestockRound);
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("재입고 알림 발송 상태 IN_PROGRESS로 변경 완료!");

        // ProductUserNotification 에서 유저 리스트 가져오기
        List<ProductUserNotification> userList = productUserNotificationRepository.findByProductIdOrderByIdAsc(productId);
        System.out.println("userList 가져오기 완료!");

        // 각 유저에게 알림 발송
        for (ProductUserNotification productUserNotification : userList) {
            // 재고가 없는 경우 (OUT_OF_STOCK)
            if (product.getStockStatus() == Product.StockStatus.OUT_OF_STOCK) {
                // CANCELED_BY_SOLD_OUT 설정, 마지막 아이디 저장
                notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_SOLD_OUT);
                productNotificationHistoryRepository.save(notificationHistory);
                return;
            }
            try {
                // 재고가 있는동안 알림 발송 : ProductUserNotificationHistory 저장
                ProductUserNotificationHistory productUserNotificationHistory = new ProductUserNotificationHistory ();
                productUserNotificationHistory.setProduct(product);
                productUserNotificationHistory.setProductUserNotification(productUserNotification);
                productUserNotificationHistory.setRestockRound(product.getRestockRound());
                productUserNotificationHistory.setNotificationDate(java.time.LocalDateTime.now());
                productUserNotificationHistoryRepository.save(productUserNotificationHistory);

                // 마지막 유저 아이디 저장
                notificationHistory.setLastNotificationUserId(productUserNotification.getId());
                productNotificationHistoryRepository.save(notificationHistory);
                System.out.println(productUserNotification.getId() + "번째 유저에게 알림 전송 완료!");

            } catch (Exception e) {
                // 에러가 나는 경우에도 마지막 유저 아이디 저장
                notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_ERROR);
                notificationHistory.setRestockRound(nextRestockRound); // 이전 회차 값 유지
                notificationHistory.setProduct(product);
                notificationHistory.setLastNotificationUserId(productUserNotification.getId());
                productNotificationHistoryRepository.save(notificationHistory);
                throw new RuntimeException("알림 발송 중 오류 발생: " + e.getMessage());
            }
        }

        // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.COMPLETED);
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("sendNotification 메서드 끝");

    }

}
