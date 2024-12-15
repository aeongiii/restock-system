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
import org.springframework.web.bind.annotation.PostMapping;

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
        // 재입고 회차 +1
        Product product = updateProductRestockRound(productId);

        // 재입고 알림 발송 상태 IN_PROGRESS 으로 변경
        ProductNotificationHistory notificationHistory = updateNotificationHistory(product, productId);

        // 유저 리스트 가져오기
        List<ProductUserNotification> userList = fetchUserList(productId);

        // 각 유저에게 알림 발송 + 예외처리
        sendNotificationsToUsers(product, notificationHistory, userList);

        // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
        completeNotification(notificationHistory, product);
    }

    // (manual) 알림 발송
    public void manualSendNotification(Long productId) {
        // 기존 발송 정보 확인
        ProductNotificationHistory notificationHistory = (ProductNotificationHistory) productNotificationHistoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품에 대한 기존 발송 정보가 없습니다. productId = " + productId));

        // 마지막 발송 유저 아이디 확인
        Long lastUserId = notificationHistory.getLastNotificationUserId();

        // 상품 정보 가져오기
        Product product = notificationHistory.getProduct();

        // lastUserId 다음 유저부터 리스트 가져오기
        List<ProductUserNotification> userList = productUserNotificationRepository.findByProductIdAndIdGreaterThanOrderByIdAsc(productId, lastUserId);

        System.out.println(userList.size() + "명의 유저에게 Manual 알림 발송 시작");

        // 각 유저에게 알림 발송 + 예외처리
        try {
            sendNotificationsToUsers(product, notificationHistory, userList);
        } catch (Exception e) {
            System.out.println("Manual 알림 발송 중 오류 발생 : " + e.getMessage());
            throw new RuntimeException("Manual 알림 발송 중 오류 발생 : ", e);
        }

        // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
        completeNotification(notificationHistory, product);
    }


    // =======

    // 재입고 회차 +1
    private Product updateProductRestockRound(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId : " + productId));
        product.setRestockRound(product.getRestockRound() + 1);
        product.setStockStatus(Product.StockStatus.IN_STOCK);
        productRepository.save(product);
        System.out.println("재입고 회차 갱신");
        return product;
    }

    // 재입고 알림 발송 상태 IN_PROGRESS 로 변경
    private ProductNotificationHistory updateNotificationHistory(Product product, Long productId) {
        ProductNotificationHistory notificationHistory = (ProductNotificationHistory) productNotificationHistoryRepository.findByProductId(productId)
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

    // 유저 리스트 가져오기
    private List<ProductUserNotification> fetchUserList(Long productId) {
        List<ProductUserNotification> userList = productUserNotificationRepository.findByProductIdOrderByIdAsc(productId);
        System.out.println("userList 불러오기 완료");
        return userList;
    }

    // 각 유저에게 알림 발송 + 예외처리
    private void sendNotificationsToUsers(Product product, ProductNotificationHistory notificationHistory, List<ProductUserNotification> userList) {
        Long lastUserId = null; // 마지막 유저 ID 추적 변수
        for (ProductUserNotification userNotification : userList) {
            try {
                // 마지막 유저 ID를 추적
                lastUserId = userNotification.getId();
                // 재고가 없는 경우 처리
                if (isOutOfStock(product)) {
                    handleOutOfStock(notificationHistory, lastUserId); // 항상 최신 lastUserId 전달
                    return; // 종료
                }
                // 개별 유저에게 알림 발송 + 마지막 유저 ID 저장
                saveUserNotificationHistory(product, userNotification, notificationHistory);
                System.out.println(userNotification.getId() + "번째 유저에게 알림 전송 완료!");
            } catch (Exception e) {
                // 알림 발송 중 오류 처리 + 마지막 유저 ID 저장
                handleNotificationError(product, notificationHistory, userNotification, e);
                return; // 에러 발생 시 종료
            }
        }
    }

    // 재고 있는지 없는지 : T/F 반환
    private boolean isOutOfStock(Product product) {
        return product.getStockStatus() == Product.StockStatus.OUT_OF_STOCK;
    }

    // 재고 없는 경우 상태 저장
    private void handleOutOfStock(ProductNotificationHistory notificationHistory, Long lastUserId) {
        // 재입고 알림 상태를 CANCELED_BY_SOLD_OUT으로 설정
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_SOLD_OUT);

        // 마지막 유저 ID 저장
        notificationHistory.setLastNotificationUserId(lastUserId != null ? lastUserId : 0L); // 마지막 유저 ID가 없으면 -1L로 설정

        // 변경사항 저장
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("재고가 없어 알림 발송을 중단했습니다.");
    }

    // 개별 유저에게 알림 발송 + 마지막 유저 ID 저장
    private void saveUserNotificationHistory(Product product, ProductUserNotification userNotification, ProductNotificationHistory notificationHistory) {
        ProductUserNotificationHistory history = new ProductUserNotificationHistory();
        history.setProduct(product);
        history.setProductUserNotification(userNotification);
        history.setRestockRound(product.getRestockRound());
        history.setNotificationDate(java.time.LocalDateTime.now());
        productUserNotificationHistoryRepository.save(history);
        notificationHistory.setLastNotificationUserId(userNotification.getId());
        productNotificationHistoryRepository.save(notificationHistory);
    }

    // 알림 발송 중 오류 처리 + 마지막 유저 id 저장
    private void handleNotificationError(Product product, ProductNotificationHistory notificationHistory, ProductUserNotification userNotification, Exception e) {
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_ERROR);
        notificationHistory.setRestockRound(product.getRestockRound());
        notificationHistory.setProduct(product);
        notificationHistory.setLastNotificationUserId(userNotification.getId());
        productNotificationHistoryRepository.save(notificationHistory);
        System.err.println("알림 발송 중 오류 발생: " + e.getMessage());
    }

    // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
    private void completeNotification(ProductNotificationHistory notificationHistory, Product product) {
        if (!isOutOfStock(product)) {
            notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.COMPLETED);
            productNotificationHistoryRepository.save(notificationHistory);
            System.out.println("알림 발송이 성공적으로 완료되었습니다.");
        }
    }
}
