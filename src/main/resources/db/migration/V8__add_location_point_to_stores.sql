-- POINT 자료형을 사용한 위치 컬럼 추가
ALTER TABLE stores 
ADD COLUMN location POINT AS (POINT(longitude, latitude)) STORED COMMENT '매장 위치 (경도, 위도)';

-- location 컬럼에 공간 인덱스 추가 (성능 향상)
CREATE SPATIAL INDEX idx_stores_location ON stores(location);
