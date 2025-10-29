package com.sparta.couponpop;

import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CouponPopApplicationTests {

    @MockitoBean
    private StoreSearchRepository storeSearchRepository;

    @Test
    void contextLoads() {
    }

}
