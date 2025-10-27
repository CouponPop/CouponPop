CREATE TABLE notification_histories
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT                      NOT NULL COMMENT '알림을 받은 회원 ID',
    type           ENUM ('FCM')                NOT NULL COMMENT '알림 유형(FCM 등)',
    title          VARCHAR(255)                NOT NULL COMMENT '알림 제목',
    body           TEXT                        NOT NULL COMMENT '알림 내용',
    status         ENUM ('SUCCESS', 'FAILURE') NOT NULL COMMENT '알림 발송 상태',
    failure_reason VARCHAR(255)                NULL COMMENT '발송 실패 원인',
    created_at     DATETIME                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',

    KEY idx_notification_histories_member_id (member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='회원별 알림 발송 이력 저장 테이블';