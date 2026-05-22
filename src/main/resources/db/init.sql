-- 스키마 생성
CREATE DATABASE IF NOT EXISTS `smart_parking_system` CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;;

-- 사용자 생성, 권한 부여
CREATE USER IF NOT EXISTS 'system_user'@'localhost' IDENTIFIED BY '0220';

GRANT ALL PRIVILEGES ON `smart_parking_system`.* TO 'system_user'@'localhost';

-- 권한 다시 로드, 즉시 적용하기 위해 넣음
FLUSH PRIVILEGES;

USE `smart_parking_system`;

CREATE TABLE IF NOT EXISTS `admin`
(
    `admin_id`          VARCHAR(20) PRIMARY KEY COMMENT '관리자 아이디',
    `password`          VARCHAR(60) NOT NULL COMMENT '관리자 비밀번호',
    `admin_name`        VARCHAR(20) NOT NULL COMMENT '관리자 이름',
    `admin_email`       VARCHAR(50) NOT NULL UNIQUE COMMENT '이메일',
    `is_active`         BOOLEAN  DEFAULT TRUE COMMENT '사용여부 True 사용중, False 사용중지',
    `last_login`        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 로그인 날짜',
    `last_login_ip`     VARCHAR(45) NULL COMMENT '마지막 로그인 IP (보안용)',
    `is_password_reset` BOOLEAN  DEFAULT FALSE COMMENT '재설정후 최초 로그인 여부 True 최초, False 일반',
    `uuid`              varchar(36) null comment '로그인 상태 유지 토큰',
    `created_at`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일'
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci COMMENT '관리자';

CREATE TABLE IF NOT EXISTS `members`
(
    `mno`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '인덱스',
    `car_num`       VARCHAR(10)  NOT NULL COMMENT '차량 번호',
    `member_name`   VARCHAR(20)  NOT NULL COMMENT '이름',
    `member_phone`  VARCHAR(15)  NOT NULL COMMENT '연락처',
    `member_charge` INT UNSIGNED NOT NULL COMMENT '회원권 결제 요금',
    `start_date`    DATE         NOT NULL COMMENT '이용 시작일',
    `end_date`      DATE         NOT NULL COMMENT '이용 만료일',
    INDEX idx_date (`start_date`, `end_date`)
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci COMMENT '회원 목록';

CREATE TABLE IF NOT EXISTS `parking_history`
(
    `park_no`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '주차 기록 인덱스',
    `parking_area`  VARCHAR(10)              NOT NULL COMMENT '주차 구역 (A1 ~ A20)',
    `car_num`       VARCHAR(10)              NOT NULL COMMENT '차량번호',
    `car_type`      ENUM ('일반', '경차', '장애인') NOT NULL COMMENT '차량 종류(일반/경차/장애인)',
    `is_member`     BOOLEAN                  NOT NULL DEFAULT FALSE COMMENT '월정액 회원 유무 (True 회원, False 비회원',
    `entry_time`    DATETIME                 NOT NULL COMMENT '입차 시간',
    `exit_time`     DATETIME COMMENT '출차 시간',
    `total_minutes` INT COMMENT '총 주차 시간'
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci COMMENT '주차 기록';


CREATE TABLE IF NOT EXISTS `payment_info`
(
    `pno`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '정책 인덱스',
    `free_time`          INT UNSIGNED    NOT NULL COMMENT '무료 회차 시간',
    `basic_time`         INT UNSIGNED    NOT NULL COMMENT '기본 시간',
    `extra_time`         INT UNSIGNED    NOT NULL COMMENT '초과 시간',
    `basic_charge`       INT UNSIGNED    NOT NULL COMMENT '기본 요금',
    `extra_charge`       INT UNSIGNED    NOT NULL COMMENT '초과 시간 당 추가 요금',
    `max_charge`         INT UNSIGNED    NOT NULL COMMENT '일일 최대 요금',
    `member_charge`      INT UNSIGNED    NOT NULL COMMENT '월 회원권 요금',
    `small_car_discount` DOUBLE UNSIGNED NOT NULL COMMENT '경차 할인율',
    `disabled_discount`  DOUBLE UNSIGNED NOT NULL COMMENT '장애인 할인율',
#     `is_active`          BOOLEAN  DEFAULT TRUE COMMENT '정책 활성화 여부 True (현재) / False (이전)',
    `admin_id`           VARCHAR(20)     NOT NULL COMMENT '정책 수정한 관리자 아이디',
    `updated_at`         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '관리자 정책 수정일',
    CONSTRAINT `fk_admin_id` FOREIGN KEY (`admin_id`) REFERENCES admin (`admin_id`)
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci COMMENT '정책';


CREATE TABLE IF NOT EXISTS `payment_history`
(
    `pay_no`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '요금 기록 인덱스',
    `parking_area`    VARCHAR(10)  NOT NULL COMMENT '주차 구역 (A1 ~ A20)',
    `car_num`         VARCHAR(10)  NOT NULL COMMENT '차량번호',
    `entry_time`      DATETIME     NOT NULL COMMENT '입차 시간',
    `exit_time`       DATETIME     NOT NULL COMMENT '출차 시간',
    `total_minutes`   INT UNSIGNED NOT NULL COMMENT '총 주차 시간',
    `total_charge`    INT UNSIGNED DEFAULT 0 COMMENT '총 요금',
    `mno`             BIGINT COMMENT '회원 번호',
    `pno`             BIGINT COMMENT '요금 정책 번호',
    `park_no`         BIGINT COMMENT '주차 기록 번호',
    `discount_amount` INT UNSIGNED DEFAULT 0 COMMENT '할인 금액',
    `final_charge`    INT UNSIGNED DEFAULT 0 COMMENT '결제 요금',
    `is_paid`         BOOLEAN      DEFAULT FALSE COMMENT '결제 여부',
    `payment_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '결제 시간',
    CONSTRAINT `fk_pno` FOREIGN KEY (`pno`) REFERENCES payment_info (`pno`),
    CONSTRAINT `fk_park_no` FOREIGN KEY (`park_no`) REFERENCES parking_history (`park_no`),
    CONSTRAINT `fk_mno` FOREIGN KEY (`mno`) REFERENCES members (`mno`)
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci COMMENT '주차 요금';


CREATE TABLE IF NOT EXISTS `validation`
(
    `no`           INT PRIMARY KEY AUTO_INCREMENT COMMENT '인덱스',
    `admin_id`     VARCHAR(20) NOT NULL COMMENT 'FK 관리자 아이디',
    `otp_code`     CHAR(6)     NOT NULL COMMENT 'OTP',
    `admin_email`  VARCHAR(50) NOT NULL COMMENT '관리자 이메일 (OTP보낸 이메일)',
    `expired_time` DATETIME    NOT NULL COMMENT '만료시간',
    CONSTRAINT fk_admin_id_otp
        FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci
    COMMENT 'OTP 로그';


