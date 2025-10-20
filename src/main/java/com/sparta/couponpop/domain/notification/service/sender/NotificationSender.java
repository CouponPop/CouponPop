package com.sparta.couponpop.domain.notification.service.sender;

import com.sparta.couponpop.domain.notification.dto.command.NotificationCommand;
import com.sparta.couponpop.domain.notification.enums.NotificationType;

/**
 * 알림 발송 인터페이스
 *
 * @param <C> 알림 실행에 필요한 커맨드 타입
 */
public interface NotificationSender<C extends NotificationCommand> {

    NotificationType getType();

    void send(C command);
}

