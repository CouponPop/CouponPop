package com.sparta.couponpop;

import com.sparta.couponpop.common.config.RedisTestContainersConfig;
import com.sparta.couponpop.common.elasticsearch.repository.CouponUsageRepository;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(RedisTestContainersConfig.class)
@SpringBootTest
class CouponPopApplicationTests {

    @MockitoBean
    private CouponUsageRepository couponUsageRepository;

    @MockitoBean
    private StoreSearchRepository storeSearchRepository;

    @Test
    void contextLoads() {
    }

}
