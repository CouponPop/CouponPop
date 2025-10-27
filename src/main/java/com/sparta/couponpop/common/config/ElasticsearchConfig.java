package com.sparta.couponpop.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 설정을 제공합니다.
 */
@Configuration
@EnableElasticsearchRepositories
public class ElasticsearchConfig {

}

