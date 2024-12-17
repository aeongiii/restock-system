-- 데이터베이스 사용 설정
USE restock_system;

-- product 테이블 생성
CREATE TABLE product (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY, -- PK : 상품 아이디
                         restock_round BIGINT NOT NULL,        -- 재입고 회차
                         stock_status ENUM('IN_STOCK', 'OUT_OF_STOCK') NOT NULL -- 재고 상태
);

-- product_notification_history 테이블 생성
CREATE TABLE product_notification_history (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,                -- PK : 히스토리 아이디
                                              product_id BIGINT NOT NULL,                          -- FK : 상품 아이디
                                              restock_round BIGINT NOT NULL,                       -- 재입고 회차
                                              restock_notification_status ENUM(
        'IN_PROGRESS',
        'CANCELED_BY_SOLD_OUT',
        'CANCELED_BY_ERROR',
        'COMPLETED'
    ) NOT NULL,                                          -- 재입고 알림 발송 상태
                                              last_notification_user_id BIGINT NOT NULL,           -- 마지막 발송 유저 아이디
                                              CONSTRAINT fk_product_notification_history_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- product_user_notification 테이블 생성
CREATE TABLE product_user_notification (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,       -- PK : 유저 알림 아이디
                                           product_id BIGINT NOT NULL,                 -- FK : 상품 아이디
                                           is_active BOOLEAN NOT NULL,                 -- 활성화 여부
                                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 날짜
                                           updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP, -- 수정 날짜
                                           CONSTRAINT fk_product_user_notification_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- product_user_notification_history 테이블 생성
CREATE TABLE product_user_notification_history (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,       -- PK : 알림 히스토리 아이디
                                                   product_id BIGINT NOT NULL,                 -- FK : 상품 아이디
                                                   user_id BIGINT NOT NULL,                    -- FK : 유저 아이디
                                                   restock_round BIGINT NOT NULL,              -- 재입고 회차
                                                   notification_date DATETIME NOT NULL,        -- 발송 날짜
                                                   CONSTRAINT fk_product_user_notification_history_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_product_user_notification_history_user FOREIGN KEY (user_id) REFERENCES product_user_notification(id) ON DELETE CASCADE
);

-- product 테이블 데이터 삽입
INSERT INTO product (restock_round, stock_status)
VALUES
    (0, 'IN_STOCK'),
    (0, 'OUT_OF_STOCK'),
    (0, 'IN_STOCK');

-- product_user_notification 테이블 데이터 삽입
INSERT INTO product_user_notification (product_id, id, is_active, created_at, updated_at)
VALUES
    (1, 1, TRUE, '2024-12-14 10:00:00', NULL),
    (1, 2, TRUE, '2024-12-14 10:05:00', NULL),
    (1, 3, TRUE, '2024-12-14 10:10:00', NULL),
    (1, 4, TRUE, '2024-12-14 10:15:00', NULL),
    (1, 5, TRUE, '2024-12-14 10:20:00', NULL),
    (2, 11, TRUE, '2024-12-14 10:50:00', NULL),
    (3, 21, TRUE, '2024-12-14 11:40:00', NULL);

-- product_notification_history 테이블 데이터 삽입
INSERT INTO product_notification_history (product_id, restock_round, restock_notification_status, last_notification_user_id)
VALUES
    (1, 1, 'IN_PROGRESS', 5),
    (2, 1, 'COMPLETED', 10),
    (3, 2, 'CANCELED_BY_SOLD_OUT', 15);

-- product_user_notification_history 테이블 데이터 삽입
INSERT INTO product_user_notification_history (product_id, user_id, restock_round, notification_date)
VALUES
    (1, 1, 1, '2024-12-14 10:00:00'),
    (2, 2, 1, '2024-12-14 10:30:00'),
    (3, 3, 2, '2024-12-14 11:00:00');

-- product_user_notification 테이블 인덱스 설정
CREATE INDEX idx_product_user_notification_product_id_id
    ON product_user_notification (product_id, id);
