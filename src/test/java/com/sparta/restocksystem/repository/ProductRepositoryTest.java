package com.sparta.restocksystem.repository;

import com.sparta.restocksystem.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest // JPA 관련 테스트를 위한 설정. 내장 H2 를 사용한다.
             // 꼭 spring.profiles.active=h2 로 변경하고 테스트하기!!
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testSaveAndFindProduct() {
        // Given
        Product product = new Product();
        product.setRestockRound(1L);
        product.setStockStatus(Product.StockStatus.IN_STOCK);
        System.out.println("Saved Product: " + product); // 저장된 Product 로그 출력

        // When : Repository의 save와 findById 기능을 테스트
        Product savedProduct = productRepository.save(product); // product를 데이터베이스에 저장
        Product foundProduct = productRepository.findById(savedProduct.getId())
                .orElse(null); // 저장된 product를 Id로 조회한다. 없다면 null 반환

        // Then
        assertNotNull(foundProduct); // 조회된 foundProduct가 null인지 아닌지 확인
        assertEquals(1L, foundProduct.getRestockRound()); // 저장된 product.RestockRound가 1인지 확인
    }
}
