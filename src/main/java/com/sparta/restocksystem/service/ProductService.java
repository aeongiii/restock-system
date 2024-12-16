package com.sparta.restocksystem.service;

import com.sparta.restocksystem.entity.Product;
import com.sparta.restocksystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 재입고 회차 +1
    @Transactional
    public Product updateProductRestockRound(Long productId) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId : " + productId));
        product.setRestockRound(product.getRestockRound() + 1);
        product.setStockStatus(Product.StockStatus.IN_STOCK);
        productRepository.save(product);
        System.out.println("재입고 회차 갱신");
        return product;
    }

    // 재고 있는지 없는지 : T/F 반환
    @Transactional(readOnly = true)
    public boolean isOutOfStock(Product product) {
        return product.getStockStatus() == Product.StockStatus.OUT_OF_STOCK;
    }

}
