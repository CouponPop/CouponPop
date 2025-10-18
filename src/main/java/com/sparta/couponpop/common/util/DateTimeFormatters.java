package com.sparta.couponpop.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeFormatters {
    public static final DateTimeFormatter KOREAN_DATE_TIME = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분");
}
