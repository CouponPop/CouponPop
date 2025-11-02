package com.sparta.couponpop.domain.fcmtoken.service;

import com.sparta.couponpop.common.dto.fcmtoken.request.FcmTokenExpireRequest;

public interface FcmTokenInternalService {

    void expireFcmToken(FcmTokenExpireRequest fcmTokenExpireRequest);
}
