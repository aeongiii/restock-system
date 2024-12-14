package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.entity.ProductUserNotification;
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

    public void sendNotification(Long productId) {

        // Product 테이블 수정 : 재입고 회차 +1
        Product product = productNotificationHistoryRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId : " + productId)).getProduct();
        Long nextRestockRound = (product.getRestockRound() + 1);
        product.setRestockRound(nextRestockRound);
        productRepository.save(product);
//        System.out.println("재입고 회차 +1 완료 !");

        // ❌알림 보내기 전에, 재고 수량 유효한지 확인 + 유효하면 재고 상태 변경

        // user를 id 정렬하여 리스트 생성
        List<ProductUserNotification> userList = productUserNotificationRepository.findByProductIdOrderByIdAsc(productId);

        // ProductNotificationHistory 테이블 수정 : 재입고 알림 발송 상태 IN_PROGRESS 으로 변경 + 회차 변경
        ProductNotificationHistory notificationHistory = (ProductNotificationHistory) productNotificationHistoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품에 대한 히스토리가 존재하지 않습니다. productId : " + productId));

        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.IN_PROGRESS);
        notificationHistory.setProduct(product);
        notificationHistory.setRestockRound(nextRestockRound);
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("재입고 알림 발송 상태 IN_PROGRESS로 변경 완료!");

        // 재고가 있는동안 알림 발송 : ProductUserNotificationHistory 저장

        // 재고가 없거나 user 다 돌았으면 발송 중단, 마지막 발송 유저아이디 저장

        // 에러가 났을 경우에도 마지막 발송 유저아이디는 저장해야 한다.

        // 해당 상품의 전송 상태 변경
        // COMPLETED / CANCELED_BY_SOLD_OUT  / CANCELED_BY_ERROR

//        회차 모두 업데이트 해야한다 다같이
    }

}
