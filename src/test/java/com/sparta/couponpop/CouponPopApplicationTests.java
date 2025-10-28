package com.sparta.couponpop;

import com.sparta.couponpop.common.elasticsearch.repository.CouponUsageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponPopApplicationTests {

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Test
    void contextLoads() {
    }

}
