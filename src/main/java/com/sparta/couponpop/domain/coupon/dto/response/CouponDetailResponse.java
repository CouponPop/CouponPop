package com.sparta.couponpop.domain.coupon.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.store.entity.Store;

import java.time.LocalDateTime;
import java.util.Optional;


public record CouponDetailResponse(
        Long id,
        CouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expireAt,
        LocalDateTime usedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        QrCode qrCode,
        EventInfo event,
        StoreInfo store
) {
    public static CouponDetailResponse from(Coupon coupon, Optional<String> tempCode) {
        return new CouponDetailResponse(
                coupon.getId(),
                coupon.getCouponStatus(),
                coupon.getReceivedAt(),
                coupon.getExpireAt(),
                coupon.getUsedAt(),
                tempCode.map(code -> new QrCode("https://couponpop.com/q/" + code, "사장님께 QR코드를 보여주세요"))
                        .orElse(null),
                EventInfo.from(coupon.getCouponEvent()),
                StoreInfo.from(coupon.getCouponEvent().getStore())
        );
    }

    public record QrCode(String url, String message) {
    }


    public record EventInfo(Long id, String name, EventPeriod period) {
        public static EventInfo from(CouponEvent event) {
            return new EventInfo(event.getId(), event.getName(),
                    new EventPeriod(event.getEventStartAt(), event.getEventEndAt()));
        }

        public record EventPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        }
    }

    public record StoreInfo(Long id, String name, String address, String phone) {
        public static StoreInfo from(Store store) {
            return new StoreInfo(store.getId(), store.getName(), store.getAddress(), store.getPhone());
        }
    }
}
