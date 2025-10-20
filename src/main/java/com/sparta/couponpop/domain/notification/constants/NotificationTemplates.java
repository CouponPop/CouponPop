package com.sparta.couponpop.domain.notification.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationTemplates {

    // 쿠폰 수령 알림 템플릿
    public static final String COUPON_ISSUED_TITLE = "쿠폰 수령이 완료되었습니다!";
    public static final String COUPON_ISSUED_BODY = """
            쿠폰명: %s
            쿠폰코드: %s
            만료기간: %s
            """;

}
