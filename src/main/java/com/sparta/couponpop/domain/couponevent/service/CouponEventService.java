package com.sparta.couponpop.domain.couponevent.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.enums.CouponStatus;
import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.sparta.couponpop.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.sparta.couponpop.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final StoreRepository storeRepository;

    private static final long MAX_EVENT_HOURS = 48L; // 이벤트 최대 기간(시간)

    @Transactional
    public CreateCouponEventResponse createCouponEvent(CreateCouponEventRequest request, Long userId) {
        /*
        TODO: StoreErrorCode 정의되면 변경하기. 사실 이 부분은 couponEvent 입장에선 매장 도메인에 요청을 해서 검증이 끝난 매장 entity 를 받아야 할듯
         */
        // store 소유 여부 검증
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        if (!store.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 매장은 로그인한 회원 소유가 아닙니다.");
        }

        // 이벤트 기간 검증
        validateEventDuration(request.eventStartAt(), request.eventEndAt());

        CouponEvent couponEvent = couponEventRepository.save(request.toEntity(store));
        return CreateCouponEventResponse.from(couponEvent);
    }

    public CouponEventDetailResponse getCouponEvent(Long eventId, Long loginUserId, LocalDateTime now) {
        CouponEvent couponEvent = couponEventRepository.findByIdWithStoreAndMember(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));
        // CouponEvent 소유 여부 검증
        couponEvent.validateOwner(loginUserId);
        int usedCouponCount = couponRepository.countByEventIdAndStatus(eventId, CouponStatus.USED);
        return CouponEventDetailResponse.of(couponEvent, usedCouponCount, now);
    }

    private void validateEventDuration(LocalDateTime start, LocalDateTime end) {
        // "이벤트 종료 시간은 시작 시간보다 이후여야 합니다."
        if (end.isBefore(start)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_END_BEFORE_START);
        }

        // "쿠폰 이벤트는 최대 48시간까지 생성 가능합니다."
        long hours = Duration.between(start, end).toHours();
        if (hours > MAX_EVENT_HOURS) {
            throw new GlobalException(CouponEventErrorCode.EVENT_DURATION_EXCEEDED);
        }

    }
}
