package com.sparta.couponpop;

import com.sparta.couponpop.common.elasticsearch.repository.CouponUsageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CouponPopApplicationTests {

    @MockitoBean
    private CouponUsageRepository couponUsageRepository;

    @Test
    void contextLoads() {
    }

}
