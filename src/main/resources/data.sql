-- 테스트용 Store 데이터
INSERT IGNORE INTO stores (business_number, store_name, owner_name, phone_number, address, category, status, created_at, updated_at) 
VALUES ('123-45-67890', '테스트 카페', '김영희', '02-1234-5678', '서울시 강남구 테헤란로 123', 'CAFE', 'ACTIVE', NOW(), NOW());