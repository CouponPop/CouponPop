package com.sparta.couponpop.domain.coupon.service.coupon_issue;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.coupon.entity.Coupon;
import com.sparta.couponpop.domain.coupon.exception.CouponErrorCode;
import com.sparta.couponpop.domain.coupon.repository.db.CouponRepository;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.exception.CouponEventErrorCode;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultCouponIssueService implements CouponIssueFacade {

    private final MemberRepository memberRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;

    @Transactional
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        // 이벤트 기간 검증
        CouponEvent event = validateEventPeriodAndTime(eventId, currentDateTime);
        // 쿠폰 중복 수령 검증
        validateCouponDuplication(memberId, eventId);
        // 쿠폰 발급 및 쿠폰 저장
        saveCouponIssue(memberId, currentDateTime, event);
    }

    public CouponEvent validateEventPeriodAndTime(Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        event.validateIssuable(currentDateTime);
        return event;
    }

    public void validateCouponDuplication(Long memberId, Long eventId) {
        if (couponRepository.existsByMemberIdAndCouponEventId(memberId, eventId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED);
        }
    }

    public void saveCouponIssue(Long memberId, LocalDateTime currentDateTime, CouponEvent event) {
        event.issueCoupon();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        Coupon issuedCoupon = Coupon.createIssuedCoupon(member, event, currentDateTime);
        couponRepository.save(issuedCoupon);
    }
}
