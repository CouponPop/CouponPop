-- aggregated_at 컬럼을 먼저 NULL 허용으로 추가
ALTER TABLE coupon_usage_stats
    ADD COLUMN aggregated_at DATE NULL;

-- 기존 데이터에 대해 aggregated_at 컬럼을 생성일 -1일로 채우기
UPDATE coupon_usage_stats
SET aggregated_at = DATE_SUB(created_at, INTERVAL 1 DAY)
WHERE aggregated_at IS NULL;

-- 컬럼을 NOT NULL로 변경
ALTER TABLE coupon_usage_stats
    MODIFY COLUMN aggregated_at DATE NOT NULL COMMENT '집계 날짜';
