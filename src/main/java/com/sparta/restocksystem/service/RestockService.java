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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Service
@Transactional
public class RestockService {

    private final ProductService productService;
    private final NotificationHistoryService notificationHistoryService;
    private final UserNotificationService userNotificationService;

    public RestockService(ProductService productService,
                          NotificationHistoryService notificationHistoryService,
                          UserNotificationService userNotificationService) {
        this.productService = productService;
        this.notificationHistoryService = notificationHistoryService;
        this.userNotificationService = userNotificationService;
    }

    // 알림 발송
    public void sendNotification(Long productId) {
        // 재입고 회차 +1
        Product product = productService.updateProductRestockRound(productId);

        // 재입고 알림 발송 상태 IN_PROGRESS 으로 변경
        ProductNotificationHistory notificationHistory = notificationHistoryService.updateNotificationHistory(product, productId);

        // 유저 리스트 가져오기
        List<ProductUserNotification> userList = userNotificationService.fetchUserList(productId);

        // 🌟🌟🌟각 유저에게 알림 발송 + 예외처리
        userNotificationService.sendNotificationsToUsers(product, notificationHistory, userList);

        // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
        notificationHistoryService.completeNotification(notificationHistory, product);
    }

    // (manual) 알림 발송
    public void manualSendNotification(Long productId) {
        // 재입고 회차 +1
        Product product = productService.updateProductRestockRound(productId);

        // 기존 발송 정보 확인
        ProductNotificationHistory notificationHistory = notificationHistoryService.getNotificationHistoryWithLock(productId);

        // 마지막 발송 유저 아이디 확인
        Long lastUserId = notificationHistory.getLastNotificationUserId();

        // lastUserId 다음 유저부터 리스트 가져오기
        List<ProductUserNotification> userList = userNotificationService.fetchUserListAfterLastUser(productId, lastUserId);

        System.out.println(userList.size() + "명의 유저에게 Manual 알림 발송 시작");

        // 각 유저에게 알림 발송 + 예외처리
        try {
            userNotificationService.sendNotificationsToUsers(product, notificationHistory, userList);
        } catch (Exception e) {
            System.out.println("Manual 알림 발송 중 오류 발생 : " + e.getMessage());
            throw new RuntimeException("Manual 알림 발송 중 오류 발생 : ", e);
        }

        // 모든 유저에게 알림 발송 성공 시 상태 COMPLETED로 변경
        notificationHistoryService.completeNotification(notificationHistory, product);
    }

}
