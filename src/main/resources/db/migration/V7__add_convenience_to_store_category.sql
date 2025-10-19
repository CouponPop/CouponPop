-- CONVENIENCE 카테고리를 store_category ENUM에 추가
ALTER TABLE stores MODIFY COLUMN store_category ENUM('CAFE', 'FOOD', 'CONVENIENCE') NOT NULL COMMENT '매장 카테고리';
