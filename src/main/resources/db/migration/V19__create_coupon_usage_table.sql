CREATE TABLE coupon_usage
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT       NOT NULL,
    coupon_id BIGINT       NOT NULL,
    store_id  BIGINT       NOT NULL,
    dong      VARCHAR(255) NOT NULL COMMENT '쿠폰 사용 매장 동 정보',
    used_at   DATETIME     NOT NULL COMMENT '쿠폰 사용 일시',

    INDEX idx_member_id (member_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_store_id (store_id),
    INDEX idx_used_at_member (used_at, member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='쿠폰 사용 이력 테이블'
