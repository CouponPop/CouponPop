package com.sparta.couponpop.domain.notification.config;

import com.sparta.couponpop.domain.notification.dto.command.NotificationCommand;
import com.sparta.couponpop.domain.notification.enums.NotificationType;
import com.sparta.couponpop.domain.notification.service.sender.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class NotificationConfig {

    @Bean
    public Map<NotificationType, NotificationSender<? extends NotificationCommand>> notificationSenderRegistry(
            List<NotificationSender<? extends NotificationCommand>> senders
    ) {
        return senders.stream().collect(Collectors.toUnmodifiableMap(NotificationSender::getType, Function.identity()));
    }
}

