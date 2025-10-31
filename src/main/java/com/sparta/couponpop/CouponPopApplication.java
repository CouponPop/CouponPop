package com.sparta.couponpop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableElasticsearchRepositories(basePackages = "com.sparta.couponpop.domain.store.repository")
public class CouponPopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponPopApplication.class, args);
    }

}
