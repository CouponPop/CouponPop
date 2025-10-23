package com.sparta.couponpop.domain.notificationhistory.service;

import com.sparta.couponpop.domain.notificationhistory.dto.payload.NotificationHistoryPayload;
import com.sparta.couponpop.domain.notificationhistory.entity.NotificationHistory;
import com.sparta.couponpop.domain.notificationhistory.repository.NotificationHistoryBulkRepository;
import com.sparta.couponpop.domain.notificationhistory.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationHistoryBulkRepository notificationHistoryBulkRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotificationHistory(NotificationHistoryPayload payload) {
        NotificationHistory notificationHistory = NotificationHistory.of(payload.memberId(), payload.type(), payload.title(), payload.body(), payload.status(), payload.failureReason());
        notificationHistoryRepository.save(notificationHistory);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkInsertNotificationHistories(List<NotificationHistoryPayload> historyPayloads) {
        List<NotificationHistory> notificationHistories = historyPayloads.stream()
                .map(payload -> NotificationHistory.of(payload.memberId(), payload.type(), payload.title(), payload.body(), payload.status(), payload.failureReason()))
                .toList();

        notificationHistoryBulkRepository.bulkInsert(notificationHistories);
    }
}
