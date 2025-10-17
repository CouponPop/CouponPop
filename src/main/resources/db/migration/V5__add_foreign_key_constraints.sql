-- Store 테이블에 member_id 외래키 제약 조건 추가
ALTER TABLE stores 
ADD CONSTRAINT fk_stores_member_id 
FOREIGN KEY (member_id) REFERENCES members(id);
