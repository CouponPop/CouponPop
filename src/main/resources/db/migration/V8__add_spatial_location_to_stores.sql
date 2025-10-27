-- POINT 컬럼 추가 (SRID 4326, NOT NULL)
ALTER TABLE stores
ADD COLUMN location POINT NOT NULL SRID 4326
    COMMENT '매장 위치 (경도, 위도)';

-- 기존 데이터가 있다면 좌표를 POINT로 업데이트
UPDATE stores
SET location = ST_SRID(POINT(longitude, latitude), 4326)
WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- 공간 인덱스 생성
CREATE SPATIAL INDEX idx_stores_location ON stores(location);
