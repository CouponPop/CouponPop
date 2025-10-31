package com.sparta.couponpop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.sparta.couponpop.domain.store.repository")
public class CouponPopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponPopApplication.class, args);
    }

}
