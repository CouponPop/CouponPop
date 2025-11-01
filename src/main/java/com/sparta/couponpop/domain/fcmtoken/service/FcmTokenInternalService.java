package com.sparta.couponpop.domain.fcmtoken.service;

public interface FcmTokenInternalService {
    
    void expireFcmToken(String fcmToken);
}
