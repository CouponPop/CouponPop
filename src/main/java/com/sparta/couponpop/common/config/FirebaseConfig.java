package com.sparta.couponpop.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sparta.couponpop.common.exception.CommonErrorCode;
import com.sparta.couponpop.common.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.firebase-config-path}")
    private String fcmConfigPath;

    @PostConstruct
    public void init() {
        try (InputStream serviceAccount = new ClassPathResource(fcmConfigPath).getInputStream()) {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류가 발생했습니다: {}", e.getMessage());
            throw new GlobalException(CommonErrorCode.FIREBASE_INITIALIZATION_FAILED);
        }
    }

}
