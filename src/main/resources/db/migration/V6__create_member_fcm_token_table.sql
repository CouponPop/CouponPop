CREATE TABLE member_fcm_tokens
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id            BIGINT       NOT NULL COMMENT 'FCM 토큰을 보유한 회원 ID',
    fcm_token            VARCHAR(255) NOT NULL COMMENT 'FCM에서 발급된 디바이스 토큰 값',
    device_type          VARCHAR(32)  NOT NULL COMMENT '디바이스 유형(ANDROID, IOS, WEB 등)',
    device_identifier    VARCHAR(128) NOT NULL COMMENT '디바이스 고유 식별자 또는 로컬 키',
    notification_enabled BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '사용자 푸시 수신 여부',
    last_used_at         DATETIME     NULL COMMENT '푸시 발송 성공 또는 앱 사용 기준 최근 사용 시각',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',

    UNIQUE KEY uk_member_fcm_token_member_device (member_id, device_identifier),
    UNIQUE KEY uk_member_fcm_token_fcm_token (fcm_token),

    FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='회원별 디바이스 FCM 토큰 정보 저장 테이블';