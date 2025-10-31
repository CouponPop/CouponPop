package com.sparta.couponpop.common.elasticsearch.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "coupon_usage")
public class CouponUsageDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long, name = "member_id")
    private Long memberId;

    @Field(type = FieldType.Long, name = "coupon_id")
    private Long couponId;

    @Field(type = FieldType.Long, name = "store_id")
    private Long storeId;

    @Field(type = FieldType.Keyword)
    private String dong;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, name = "used_at")
    private LocalDateTime usedAt;

    private CouponUsageDocument(Long memberId, Long couponId, Long storeId, String dong, LocalDateTime usedAt) {
        this.memberId = memberId;
        this.couponId = couponId;
        this.storeId = storeId;
        this.dong = dong;
        this.usedAt = usedAt;
    }

    public static CouponUsageDocument create(Long memberId, Long couponId, Long storeId, String dong, LocalDateTime usedAt) {
        return new CouponUsageDocument(memberId, couponId, storeId, dong, usedAt);
    }
}
