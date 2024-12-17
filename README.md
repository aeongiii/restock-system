# 재입고 알림 시스템

## 1. 프로젝트 소개

**재입고 알림 시스템**은 상품이 재입고되었을 때, 해당 상품의 알림을 설정한 사용자에게 알림 메시지를 전송하는 시스템입니다. 재고가 소진될 경우 알림 발송을 중단하며, 회차별 알림 발송 상태를 기록합니다.

---

## 2. 기술 스택

- **Backend**: Spring Boot , Java 17
- **Database**: MySQL 8.0
- **Build & Deployment**: Gradle, Docker Compose
- **Libraries**:
    - Google Guava: `RateLimiter`를 이용한 초당 요청 제한
    - Spring Data JPA: 데이터 접근 계층 관리
- **Testing**: JUnit 5, Mockito

---

## 3. 핵심 기능

1. **재입고 알림 발송**
    - 상품의 재입고 회차를 증가시킨 후, 알림을 설정한 유저들에게 알림 메시지를 순차적으로 전송합니다.
    - 알림 전송 시 초당 **500개** 요청 제한을 적용합니다.
2. **재고 소진 시 알림 중단**
    - 알림 전송 중 재고가 소진되면 알림 전송을 중단하고 상태를 기록합니다.
3. **알림 발송 이력 저장**
    - 상품별 재입고 회차, 알림 상태, 마지막 발송 유저 정보를 기록합니다.
    - 각 유저에게 발송된 알림 이력도 저장합니다.
4. **재전송 API**
    - 예외로 인해 알림이 중단된 경우, 마지막 성공 유저 이후부터 알림을 재발송할 수 있습니다.

---

## 4. 데이터베이스 설계

| **Product** (상품 테이블) | **타입** | **설명** |
| --- | --- | --- |
| id | BIGINT | PK, 상품 아이디 |
| restock_round | BIGINT | 재입고 회차 |
| stock_status | BIGINT | 재고 상태 (숫자 타입) |

| **ProductNotificationHistory** (알림 히스토리) | **타입** | **설명** |
| --- | --- | --- |
| id | BIGINT | PK, 자동 증가 |
| product_id | BIGINT | 상품 ID (FK) |
| restock_round | BIGINT | 재입고 회차 |
| restock_notification_status | VARCHAR | 발송 상태(IN_PROGRESS 등) |
| last_notification_user_id | BIGINT | 마지막 알림 발송 유저 ID |

| **ProductUserNotification** (알림 설정 유저) | **타입** | **설명** |
| --- | --- | --- |
| id | BIGINT | PK, 자동 증가 |
| product_id | BIGINT | 상품 ID (FK) |
| user_id | BIGINT | 유저 ID |
| is_active | BOOLEAN | 알림 활성화 여부 |
| created_at | TIMESTAMP | 생성 날짜 |
| updated_at | TIMESTAMP | 수정 날짜 |

| **ProductUserNotificationHistory** (유저별 알림 이력) | **타입** | **설명** |
| --- | --- | --- |
| id | BIGINT | PK, 자동 증가 |
| product_id | BIGINT | 상품 ID (FK) |
| user_id | BIGINT | 유저 ID |
| restock_round | BIGINT | 재입고 회차 |
| notification_date | TIMESTAMP | 알림 발송 날짜 |

---

## 5. 기술적 의사결정

### 1. **알림 발송 속도 제한**

알림 메시지는 초당 500개까지만 전송해야 하는 비즈니스 요구사항이 있었습니다.

- **RateLimiter**를 사용하여 초당 요청 제한을 안정적으로 구현했습니다.

### 2. **트랜잭션 관리 최적화**

- 클래스 레벨 `@Transactional` 및 `@Transactional(readOnly = true)`를 적절히 사용해 트랜잭션 효율성을 개선했습니다.

### 3. **비관적 락을 통한 동시성 제어**

- `Pessimistic Lock`을 적용하여 재입고 회차와 알림 상태의 정합성을 보장했습니다.

### 4. **트랜잭션 범위 최소화**

- 서비스 메서드 분리를 통해 트랜잭션 범위를 최소화했습니다.

### 5. **인덱스 최적화**

- 복합 인덱스를 사용해 대용량 데이터 조회 성능을 극대화했습니다.

---

## 6. 실행 방법

### **1. 환경 준비**
- **Docker**와 **Docker Compose**가 설치되어 있어야 합니다.
    - Docker 설치: [링크](https://docs.docker.com/get-docker/)
    - Docker Compose 설치: [링크](https://docs.docker.com/compose/install/)

---

### **2. 소스 코드 가져오기**
```bash
git clone <레포지토리 URL>
cd <프로젝트 폴더>


---

- DB는 컨테이너 시작 시 자동으로 생성됩니다.
- 애플리케이션은 http://localhost:8083/ 에서 실행됩니다.
