package com.sparta.couponpop.domain.coupon.service;

import com.sparta.couponpop.common.elasticsearch.repository.CouponUsageRepository;
import com.sparta.couponpop.config.RedisTestContainersConfig;
import com.sparta.couponpop.domain.coupon.repository.db.CouponRepository;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Import(RedisTestContainersConfig.class)
@ActiveProfiles("test-concurrency")
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class CouponIssueConcurrencyTestSupport {

    @MockitoBean
    private CouponUsageRepository couponUsageRepository;
    @MockitoBean
    private StoreSearchRepository storeSearchRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected StoreRepository storeRepository;

    @Autowired
    protected CouponEventRepository couponEventRepository;

    @Autowired
    protected CouponRepository couponRepository;

    protected Member member;
    protected Store store;
    protected CouponEvent couponEvent;

    protected static final int THREAD_COUNT = 100;
    protected static final int TOTAL_COUPON_COUNT = 100;

    protected static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 25, 12, 0);
    protected static final LocalDateTime eventStartAt = issuedTime.minusHours(1);
    protected static final LocalDateTime eventEndAt = issuedTime.plusDays(1);

    protected List<Member> members = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < THREAD_COUNT; i++) {
            member = TestUtils.createEntity(Member.class, Map.of(
                    "username", "기존이름",
                    "email", "test" + (i + 1) + "@example.com",
                    "password", "기존비밀번호",
                    "phoneNumber", "01099999999",
                    "memberType", MemberType.CUSTOMER));
            members.add(memberRepository.save(member));
        }

        store = TestUtils.createEntity(Store.class, Map.ofEntries(
                Map.entry("storeCategory", StoreCategory.FOOD),
                Map.entry("name", "storeTest"),
                Map.entry("phone", "01012341234"),
                Map.entry("description", "test"),
                Map.entry("businessNumber", "test"),
                Map.entry("address", "test"),
                Map.entry("dong", "dong"),
                Map.entry("latitude", 32.1235),
                Map.entry("longitude", 45.1634),
                Map.entry("imageUrl", "test"),
                Map.entry("weekdayOpenTime", LocalTime.of(9, 0)),
                Map.entry("weekdayCloseTime", LocalTime.of(21, 0)),
                Map.entry("weekendOpenTime", LocalTime.of(9, 0)),
                Map.entry("weekendCloseTime", LocalTime.of(21, 0)),
                Map.entry("member", member)
        ));
        storeRepository.save(store);
        couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "name", "이벤트 제목",
                "eventStartAt", eventStartAt,
                "eventEndAt", eventEndAt,
                "totalCount", TOTAL_COUPON_COUNT,
                "store", store
        ));
        couponEventRepository.save(couponEvent);
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
        couponEventRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }
}
