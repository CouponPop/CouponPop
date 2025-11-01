package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.domain.fcmtoken.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmTokenInternalServiceImpl implements FcmTokenInternalService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * TODO: Member Service의 로그아웃 API, 회원탈퇴 API 사용 필요
     * memberId는 authMember로 부터 획득
     * EDA 적용 가능
     */
    @Override
    public void expireFcmToken(String fcmToken) {

        // 기존 구현 메서드
//        fcmTokenRepository
//                .findByMemberIdAndFcmToken(memberId, fcmToken)
//                .ifPresent(fcmTokenRepository::delete);
    }
}
