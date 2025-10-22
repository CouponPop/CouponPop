-- 기존 location 컬럼 삭제 (V8에서 생성된 것)
ALTER TABLE stores DROP COLUMN location;

-- GENERATED ALWAYS AS STORED를 사용한 새로운 location 컬럼과 공간 인덱스 추가
ALTER TABLE stores
ADD COLUMN location POINT
    GENERATED ALWAYS AS (
        ST_SRID(POINT(longitude, latitude), 4326)
    ) STORED NOT NULL,
    COMMENT '매장 위치 (경도, 위도)',
ADD SPATIAL INDEX idx_stores_location (location);
