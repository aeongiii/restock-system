package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.entity.ProductNotificationHistory;
import com.sparta.restocksystem.entity.ProductUserNotification;
import com.sparta.restocksystem.entity.ProductUserNotificationHistory;
import com.sparta.restocksystem.repository.ProductNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationHistoryRepository;
import com.sparta.restocksystem.repository.ProductUserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserNotificationService {

    private final ProductUserNotificationRepository productUserNotificationRepository;
    private final ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository;
    private final ProductNotificationHistoryRepository productNotificationHistoryRepository;
    private final ProductService productService;

    public UserNotificationService(ProductUserNotificationRepository productUserNotificationRepository,
                                   ProductUserNotificationHistoryRepository productUserNotificationHistoryRepository, ProductNotificationHistoryRepository productNotificationHistoryRepository, ProductService productService) {
        this.productUserNotificationRepository = productUserNotificationRepository;
        this.productUserNotificationHistoryRepository = productUserNotificationHistoryRepository;
        this.productNotificationHistoryRepository = productNotificationHistoryRepository;
        this.productService = productService;
    }

    // 유저 리스트 가져오기
    @Transactional(readOnly = true)
    public List<ProductUserNotification> fetchUserList(Long productId) {
        return  productUserNotificationRepository.findByProductIdOrderByIdAsc(productId);
    }

    // lastUserId 다음 유저부터 리스트 가져오기
    @Transactional
    public List<ProductUserNotification> fetchUserListAfterLastUser (Long productId, Long lastUSerId) {
        return productUserNotificationRepository.findByProductIdAndIdGreaterThanOrderByIdAsc(productId, lastUSerId);
    }

    // 각 유저에게 알림 발송 + 예외처리
    public void sendNotificationsToUsers(Product product, ProductNotificationHistory notificationHistory, List<ProductUserNotification> userList) {
        int batchSize = 500; // 배치 크기 설정
        Long lastUserId = null; // 마지막 유저 ID 추적 변수

        // 전체 유저 리스트를 배치 단위로 나누어 처리
        for (int i = 0; i < userList.size(); i += batchSize) {
            // 현재 배치 추출
            List<ProductUserNotification> batch = userList.subList(i, Math.min(i + batchSize, userList.size()));

            for (ProductUserNotification userNotification : batch) {
                try {
                    // 마지막 유저 ID를 추적
                    lastUserId = userNotification.getId();
                    // 재고가 없는 경우 처리
                    if (productService.isOutOfStock(product)) {
                        handleOutOfStock(notificationHistory, lastUserId); // 항상 최신 lastUserId 전달
                        return; // 종료
                    }
                    // 개별 유저에게 알림 발송 + 마지막 유저 ID 저장
                    saveUserNotificationHistory(product, userNotification, notificationHistory);
//                    System.out.println(userNotification.getId() + "번째 유저에게 알림 전송 완료!");
                } catch (Exception e) {
                    // 알림 발송 중 오류 처리 + 마지막 유저 ID 저장
                    handleNotificationError(product, notificationHistory, userNotification, e);
                    return; // 에러 발생 시 종료
                }
            }

            // 배치 완료 후 1초 대기
            try {
                Thread.sleep(1000);
                System.out.println("500개 알림 발송 완료. 1초 대기 후 다음 배치 처리 시작");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("배치 처리 중 인터럽트 발생", e);
            }
        }

        // 마지막 유저 ID 갱신
        if (!userList.isEmpty()) {
            notificationHistory.setLastNotificationUserId(userList.get(userList.size() - 1).getId());
            productNotificationHistoryRepository.save(notificationHistory);
        }
    }


    // 재고 없는 경우 상태 저장
    public void handleOutOfStock(ProductNotificationHistory notificationHistory, Long lastUserId) {
        // 재입고 알림 상태를 CANCELED_BY_SOLD_OUT으로 설정
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_SOLD_OUT);

        // 마지막 유저 ID 저장
        notificationHistory.setLastNotificationUserId(lastUserId != null ? lastUserId : 0L); // 마지막 유저 ID가 없으면 -1L로 설정

        // 변경사항 저장
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println("재고가 없어 알림 발송을 중단했습니다.");
    }


    // 알림 발송 중 오류 처리 + 마지막 유저 id 저장
    public void handleNotificationError(Product product, ProductNotificationHistory notificationHistory, ProductUserNotification userNotification, Exception e) {
        notificationHistory.setRestockNotificationStatus(ProductNotificationHistory.RestockNotificationStatus.CANCELED_BY_ERROR);
        notificationHistory.setRestockRound(product.getRestockRound());
        notificationHistory.setProduct(product);
        notificationHistory.setLastNotificationUserId(userNotification.getId());
        productNotificationHistoryRepository.save(notificationHistory);
        System.err.println("알림 발송 중 오류 발생: " + e.getMessage());
    }



    // 개별 유저에게 알림 발송 + 마지막 유저 ID 저장
    @Transactional
    public void saveUserNotificationHistory(Product product,
                                            ProductUserNotification userNotification,
                                            ProductNotificationHistory notificationHistory) {
        ProductUserNotificationHistory history = new ProductUserNotificationHistory();
        history.setProduct(product);
        history.setProductUserNotification(userNotification);
        history.setRestockRound(product.getRestockRound());
        history.setNotificationDate(java.time.LocalDateTime.now());
        productUserNotificationHistoryRepository.save(history);

        notificationHistory.setLastNotificationUserId(userNotification.getId());
        productNotificationHistoryRepository.save(notificationHistory);
        System.out.println(userNotification.getId() + "번째 유저에게 알림 전송 완료!");
    }


}
