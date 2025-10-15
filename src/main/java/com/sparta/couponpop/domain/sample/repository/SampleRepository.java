package com.sparta.couponpop.domain.sample.repository;

import com.sparta.couponpop.domain.sample.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<Sample, Long> {
}
