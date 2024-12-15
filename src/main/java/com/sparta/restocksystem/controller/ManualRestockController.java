package com.sparta.restocksystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sparta.restocksystem.service.RestockService;


@RestController
@RequestMapping("/admin/products")
public class ManualRestockController {

    private final RestockService restockService;

    public ManualRestockController(RestockService restockService) {
        this.restockService = restockService;
    }

    // 알림 발송
    @PostMapping(value = "/{productId}/notifications/re-stock")
    public ResponseEntity<Void> manualSendNotification(@PathVariable Long productId) {

        // 서비스 호출
        restockService.manualSendNotification(productId);

        return ResponseEntity.ok().build(); // 상태코드 200 반환
    }

}
