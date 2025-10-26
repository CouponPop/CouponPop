package com.sparta.couponpop.domain.coupon.service.coupon_issue;

import com.sparta.couponpop.domain.coupon.repository.CouponRepository;
import com.sparta.couponpop.domain.couponevent.entity.CouponEvent;
import com.sparta.couponpop.domain.couponevent.repository.CouponEventRepository;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.enums.MemberType;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.enums.StoreCategory;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test-concurrency")
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponIssueConcurrencyTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private CouponEventRepository couponEventRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponIssueService couponIssueService;

    private Member member;
    private Store store;
    private CouponEvent couponEvent;

    private static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 25, 12, 0);
    private static final LocalDateTime eventStartAt = issuedTime.minusHours(1);
    private static final LocalDateTime eventEndAt = issuedTime.plusDays(1);

    @BeforeEach
    void setUp() {
        member = TestUtils.createEntity(Member.class, Map.of(
                "username", "기존이름",
                "email", "test2@example.com",
                "password", "기존비밀번호",
                "phoneNumber", "01099999999",
                "memberType", MemberType.CUSTOMER));
        memberRepository.save(member);
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
                "totalCount", 100,
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

    private static final int THREAD_COUNT = 100;

    @Test
    void 동시에_100개_요청_실패() throws InterruptedException {
        // given
        final Long eventId = couponEvent.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long currentMemberId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueService.issueCoupon(currentMemberId, eventId, issuedTime);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // when
        CouponEvent event = couponEventRepository.findById(eventId).orElseThrow();

        // then
        assertThat(event.getIssuedCount()).isNotEqualTo(100);
    }
}