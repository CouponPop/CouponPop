package com.sparta.couponpop.domain.notificationhistory.repository;

import com.sparta.couponpop.domain.notificationhistory.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
}
