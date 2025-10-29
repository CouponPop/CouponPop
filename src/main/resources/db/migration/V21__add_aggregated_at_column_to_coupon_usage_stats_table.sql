ALTER TABLE coupon_usage_stats
    ADD COLUMN aggregated_at DATE NOT NULL COMMENT '집계 날짜';