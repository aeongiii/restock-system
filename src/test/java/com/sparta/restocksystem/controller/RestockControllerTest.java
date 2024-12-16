package com.sparta.restocksystem.controller;

import com.sparta.restocksystem.service.RestockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestockController.class) // 단위테스트 설정
class RestockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // 이제 @MockitoBean으로 교체해야 한다는데... MockitoBean 쓰면 오류나서 원래대로 MockBean 썼다.
    private RestockService restockService;

    @Test
    void testSendNotification() throws Exception {
    // Given
        Long productId = 1L; // 상품 id 설정
        doNothing().when(restockService).sendNotification(productId);

    // When & Then
        // HTTP POST 요청을 모의로 보냄
        mockMvc.perform(post("/products/{productId}/notifications/re-stock", productId))
                .andExpect(status().isOk()); // 응답 코드가 200OK인지 확인한다.
    }

}
