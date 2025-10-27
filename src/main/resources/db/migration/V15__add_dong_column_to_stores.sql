-- 매장 테이블에 동(dong) 컬럼 추가
ALTER TABLE stores
ADD COLUMN dong VARCHAR(50) NOT NULL COMMENT '동 (예: 신림동)';
