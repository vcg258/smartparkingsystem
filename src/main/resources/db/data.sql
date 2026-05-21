-- admin: 1234, test: 1111, test1: test1, demo: demo1234(포트폴리오 계정)
INSERT INTO admin
(admin_id, password, admin_name, admin_email, is_active, last_login, last_login_ip, created_at)
VALUES ('admin', '$2a$12$hvk0XVGUYQk2BwV4SZ9Sz.xCrkCOCgQ3KGCv.QI77JGdJZ9Ri2usW', '관리자', 'admin@naver.com', true, NULL,
        NULL, NOW()),
       ('test', '$2a$12$D0Tcf..4G2y8woY2HgB9veF.yjdoUeiI2yMymm8xVIOLB8yv7mjmO', 'Test', 'test@naver.com', false, NULL,
        NULL, NOW()),
       ('test1', '$2a$12$teiNyPb2nUP6vMA9aoQL/OeXC01FtycxknhHtm10CSj4SU/VqQIG6', 'Test1', 'test1@naver.com', true, NULL,
        NULL, NOW()),
       ('demo', '$2a$12$qRYcQrwrNbbJzsh/7.Ng.u/AspuoePpoitMa64PJLYX6/Y4hjsg.m', '데모계정', 'demo@example.com', true, NULL,
        NULL, NOW());

-- 기준일은 실행 시점의 CURDATE()입니다.
-- 시간이 지나도 deletePrevious.sql 실행 후 다시 넣으면 최근/이번 달/과거 통계 테스트가 가능하도록 상대 날짜로 구성합니다.

-- 회원 더미: 활성, 만료, 예정 회원을 섞어서 회원 통계 필터를 확인할 수 있게 구성
INSERT INTO members
(car_num, member_name, member_phone, member_charge, start_date, end_date)
VALUES
    -- 활성 회원
    ('11가1001', '김민준', '010-1001-1001', 100000, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY)),
    ('22나1002', '이서연', '010-1002-1002', 100000, DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
    ('33다1003', '박지호', '010-1003-1003', 100000, DATE_SUB(CURDATE(), INTERVAL 75 DAY), DATE_ADD(CURDATE(), INTERVAL 75 DAY)),
    ('44라1004', '최수아', '010-1004-1004', 100000, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY)),
    ('55마1005', '정우진', '010-1005-1005', 100000, DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY)),
    ('66바1006', '강하늘', '010-1006-1006', 100000, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
    ('77사1007', '윤지민', '010-1007-1007', 100000, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY)),
    ('88아1008', '임도현', '010-1008-1008', 100000, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 80 DAY)),
    ('99자1009', '한소희', '010-1009-1009', 100000, DATE_SUB(CURDATE(), INTERVAL 150 DAY), DATE_ADD(CURDATE(), INTERVAL 210 DAY)),
    ('10차1010', '오민서', '010-1010-1010', 100000, DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
    ('21카1011', '신준혁', '010-1011-1011', 100000, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 345 DAY)),
    ('32타1012', '류지은', '010-1012-1012', 100000, DATE_SUB(CURDATE(), INTERVAL 300 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY)),

    -- 만료 회원
    ('43파2001', '배현우', '010-2001-2001', 100000, DATE_SUB(CURDATE(), INTERVAL 420 DAY), DATE_SUB(CURDATE(), INTERVAL 330 DAY)),
    ('54하2002', '노아름', '010-2002-2002', 100000, DATE_SUB(CURDATE(), INTERVAL 360 DAY), DATE_SUB(CURDATE(), INTERVAL 270 DAY)),
    ('65거2003', '문지훈', '010-2003-2003', 100000, DATE_SUB(CURDATE(), INTERVAL 300 DAY), DATE_SUB(CURDATE(), INTERVAL 210 DAY)),
    ('76너2004', '권나연', '010-2004-2004', 100000, DATE_SUB(CURDATE(), INTERVAL 240 DAY), DATE_SUB(CURDATE(), INTERVAL 150 DAY)),
    ('87더2005', '황태양', '010-2005-2005', 100000, DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 90 DAY)),
    ('98러2006', '송예진', '010-2006-2006', 100000, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
    ('19머2007', '전민호', '010-2007-2007', 100000, DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
    ('20버2008', '조하린', '010-2008-2008', 100000, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY)),

    -- 예정 회원
    ('31서3001', '남기현', '010-3001-3001', 100000, DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 97 DAY)),
    ('42어3002', '마지수', '010-3002-3002', 100000, DATE_ADD(CURDATE(), INTERVAL 14 DAY), DATE_ADD(CURDATE(), INTERVAL 104 DAY)),
    ('53저3003', '홍준서', '010-3003-3003', 100000, DATE_ADD(CURDATE(), INTERVAL 21 DAY), DATE_ADD(CURDATE(), INTERVAL 111 DAY)),
    ('64처3004', '유소현', '010-3004-3004', 100000, DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY));

-- 정책 더미: 과거 정책 1건 + 현재 정책 1건
INSERT INTO payment_info
(free_time, basic_time, extra_time, basic_charge, extra_charge, max_charge,
 small_car_discount, disabled_discount, admin_id, member_charge, updated_at)
VALUES
    (10, 60, 30, 2000, 1000, 15000, 0.30, 0.50, 'admin', 90000, DATE_SUB(NOW(), INTERVAL 180 DAY)),
    (10, 60, 30, 2500, 1000, 18000, 0.30, 0.50, 'admin', 100000, NOW());

-- 완료된 주차 이력 420건
-- 0~23번은 오늘 데이터, 나머지는 최근 약 18개월 범위에 분산됩니다.
INSERT INTO parking_history
(parking_area, car_num, car_type, is_member, entry_time, exit_time, total_minutes)
SELECT
    CONCAT('A', LPAD(MOD(base.n, 20) + 1, 2, '0')) AS parking_area,
    CASE
        WHEN MOD(base.n, 6) = 0 THEN ELT(MOD(base.n, 24) + 1,
            '11가1001', '22나1002', '33다1003', '44라1004', '55마1005', '66바1006',
            '77사1007', '88아1008', '99자1009', '10차1010', '21카1011', '32타1012',
            '43파2001', '54하2002', '65거2003', '76너2004', '87더2005', '98러2006',
            '19머2007', '20버2008', '31서3001', '42어3002', '53저3003', '64처3004')
        ELSE CONCAT(LPAD(10 + MOD(base.n * 7, 90), 2, '0'),
                    ELT(MOD(base.n, 8) + 1, '가', '나', '다', '라', '마', '바', '사', '아'),
                    LPAD(3000 + base.n, 4, '0'))
        END AS car_num,
    CASE
        WHEN MOD(base.n, 10) = 0 THEN '장애인'
        WHEN MOD(base.n, 10) IN (1, 2) THEN '경차'
        ELSE '일반'
    END AS car_type,
    MOD(base.n, 6) = 0 AS is_member,
    base.entry_time,
    DATE_ADD(base.entry_time, INTERVAL base.total_minutes MINUTE) AS exit_time,
    base.total_minutes
FROM (
    SELECT
        numbers.n,
        CASE
            WHEN numbers.n < 24 THEN DATE_SUB(NOW(), INTERVAL ((numbers.n + 3) * 30) MINUTE)
            ELSE TIMESTAMP(
                DATE_SUB(CURDATE(), INTERVAL MOD(numbers.n * 3, 540) DAY),
                SEC_TO_TIME(
                    CASE
                        WHEN MOD(numbers.n, 20) < 2 THEN 7 * 3600 + MOD(numbers.n * 11, 3600)
                        WHEN MOD(numbers.n, 20) < 7 THEN 9 * 3600 + MOD(numbers.n * 13, 3600)
                        WHEN MOD(numbers.n, 20) < 11 THEN 12 * 3600 + MOD(numbers.n * 17, 7200)
                        WHEN MOD(numbers.n, 20) < 16 THEN 18 * 3600 + MOD(numbers.n * 19, 7200)
                        ELSE 21 * 3600 + MOD(numbers.n * 23, 7200)
                    END
                )
            )
        END AS entry_time,
        CASE
            WHEN MOD(numbers.n, 12) = 0 THEN 20
            WHEN MOD(numbers.n, 12) IN (1, 2, 3) THEN 45 + MOD(numbers.n * 7, 45)
            WHEN MOD(numbers.n, 12) IN (4, 5, 6, 7) THEN 90 + MOD(numbers.n * 11, 180)
            WHEN MOD(numbers.n, 12) IN (8, 9, 10) THEN 240 + MOD(numbers.n * 13, 360)
            ELSE 900 + MOD(numbers.n * 17, 600)
        END AS total_minutes
    FROM (
        SELECT ones.i + tens.i * 10 + hundreds.i * 100 AS n
        FROM (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
              UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
        CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
        CROSS JOIN (SELECT 0 i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) hundreds
    ) numbers
    WHERE numbers.n < 420
) base
ORDER BY base.entry_time;

-- 현재 주차 중인 차량 8건: 메인 보드/출차 테스트용
INSERT INTO parking_history
(parking_area, car_num, car_type, is_member, entry_time, exit_time, total_minutes)
VALUES
    ('A01', '11가1001', '일반', TRUE, DATE_SUB(NOW(), INTERVAL 35 MINUTE), NULL, NULL),
    ('A03', '22나1002', '경차', TRUE, DATE_SUB(NOW(), INTERVAL 80 MINUTE), NULL, NULL),
    ('A05', '71나9001', '일반', FALSE, DATE_SUB(NOW(), INTERVAL 125 MINUTE), NULL, NULL),
    ('A07', '82다9002', '장애인', FALSE, DATE_SUB(NOW(), INTERVAL 190 MINUTE), NULL, NULL),
    ('A09', '93라9003', '일반', FALSE, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL, NULL),
    ('A11', '44라1004', '일반', TRUE, DATE_SUB(NOW(), INTERVAL 260 MINUTE), NULL, NULL),
    ('A13', '15마9004', '경차', FALSE, DATE_SUB(NOW(), INTERVAL 310 MINUTE), NULL, NULL),
    ('A15', '26바9005', '일반', FALSE, DATE_SUB(NOW(), INTERVAL 410 MINUTE), NULL, NULL);

-- 최신 정책 정보 로드
SELECT
    @p_no := pno,
    @f_time := free_time,
    @b_time := basic_time,
    @e_time := extra_time,
    @b_charge := basic_charge,
    @e_charge := extra_charge,
    @m_charge := max_charge,
    @s_disc := small_car_discount,
    @d_disc := disabled_discount
FROM payment_info
ORDER BY updated_at DESC
LIMIT 1;

-- 완료된 주차 이력에 대한 결제 이력 생성
INSERT INTO payment_history
(parking_area, car_num, entry_time, exit_time, total_minutes, total_charge,
 mno, pno, park_no, discount_amount, final_charge, is_paid, payment_time)
SELECT
    fee.parking_area,
    fee.car_num,
    fee.entry_time,
    fee.exit_time,
    fee.total_minutes,
    fee.raw_fee AS total_charge,
    fee.mno,
    @p_no AS pno,
    fee.park_no,
    CASE
        WHEN fee.mno IS NOT NULL THEN fee.raw_fee
        WHEN fee.car_type = '장애인' THEN FLOOR(fee.raw_fee * @d_disc)
        WHEN fee.car_type = '경차' THEN FLOOR(fee.raw_fee * @s_disc)
        ELSE 0
    END AS discount_amount,
    fee.raw_fee - CASE
        WHEN fee.mno IS NOT NULL THEN fee.raw_fee
        WHEN fee.car_type = '장애인' THEN FLOOR(fee.raw_fee * @d_disc)
        WHEN fee.car_type = '경차' THEN FLOOR(fee.raw_fee * @s_disc)
        ELSE 0
    END AS final_charge,
    TRUE AS is_paid,
    DATE_ADD(fee.exit_time, INTERVAL MOD(fee.park_no, 15) MINUTE) AS payment_time
FROM (
    SELECT
        ph.parking_area,
        ph.car_num,
        ph.car_type,
        ph.entry_time,
        ph.exit_time,
        ph.total_minutes,
        ph.park_no,
        m.mno,
        CASE
            WHEN ph.total_minutes <= @f_time THEN 0
            WHEN ph.total_minutes <= @b_time THEN @b_charge
            ELSE LEAST(@m_charge, @b_charge + (CEIL((ph.total_minutes - @b_time) / @e_time) * @e_charge))
        END AS raw_fee
    FROM parking_history ph
    LEFT JOIN members m
        ON ph.car_num = m.car_num
        AND DATE(ph.entry_time) BETWEEN m.start_date AND m.end_date
    WHERE ph.exit_time IS NOT NULL
) fee;

-- 통계에서 제외되는 미결제 샘플: is_paid = FALSE 필터 확인용
INSERT INTO payment_history
(parking_area, car_num, entry_time, exit_time, total_minutes, total_charge,
 mno, pno, park_no, discount_amount, final_charge, is_paid, payment_time)
SELECT
    ph.parking_area,
    ph.car_num,
    ph.entry_time,
    ph.exit_time,
    ph.total_minutes,
    2500,
    NULL,
    @p_no,
    ph.park_no,
    0,
    2500,
    FALSE,
    DATE_ADD(ph.exit_time, INTERVAL 5 MINUTE)
FROM parking_history ph
WHERE ph.exit_time IS NOT NULL
ORDER BY ph.park_no DESC
LIMIT 5;
