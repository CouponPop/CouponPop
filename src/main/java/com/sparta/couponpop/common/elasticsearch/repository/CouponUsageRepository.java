package com.sparta.couponpop.common.elasticsearch.repository;

import com.sparta.couponpop.common.elasticsearch.document.CouponUsageDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CouponUsageRepository extends ElasticsearchRepository<CouponUsageDocument, Long> {
}
