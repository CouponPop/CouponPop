CREATE TABLE coupon_usage_stats
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT       NOT NULL COMMENT '손님 ID',
    top_dong   VARCHAR(255) NOT NULL COMMENT '쿠폰 사용 상위 동 정보',
    top_hour   TINYINT      NOT NULL COMMENT '쿠폰 사용 상위 시간대(0~23)',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',

    UNIQUE KEY uk_member_id (member_id),

    INDEX idx_top_dong (top_dong),
    INDEX idx_top_hour (top_hour)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='손님별 쿠폰 사용 집계 테이블';