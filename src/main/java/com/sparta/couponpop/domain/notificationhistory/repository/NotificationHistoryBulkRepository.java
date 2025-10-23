package com.sparta.couponpop.domain.notificationhistory.repository;

import com.sparta.couponpop.domain.notificationhistory.entity.NotificationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationHistoryBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<NotificationHistory> notificationHistories) {
        String sql = """
                    INSERT INTO notification_histories (member_id, type, title, body, status, failure_reason, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, now())
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                NotificationHistory history = notificationHistories.get(i);
                ps.setLong(1, history.getMemberId());
                ps.setString(2, history.getType().name());
                ps.setString(3, history.getTitle());
                ps.setString(4, history.getBody());
                ps.setString(5, history.getStatus().name());
                ps.setString(6, history.getFailureReason());
            }

            @Override
            public int getBatchSize() {
                return notificationHistories.size();
            }
        });
    }

}
