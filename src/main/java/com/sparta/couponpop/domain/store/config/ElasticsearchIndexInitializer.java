package com.sparta.couponpop.domain.store.config;

import com.sparta.couponpop.domain.store.document.StoreDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 Elasticsearch 인덱스를 초기화하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 애플리케이션이 준비되면 인덱스를 재생성
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(StoreDocument.class);
            
            // 기존 인덱스 존재 여부 확인
            if (indexOps.exists()) {
                log.info("Elasticsearch index 'stores' already exists. Deleting and recreating...");
                // 기존 인덱스 삭제
                indexOps.delete();
                log.info("Deleted existing 'stores' index");
            }
            
            // 인덱스 생성
            indexOps.create();
            log.info("Created new 'stores' index");
            
            // 매핑 적용
            indexOps.putMapping(indexOps.createMapping(StoreDocument.class));
            log.info("Successfully created Elasticsearch index 'stores' with mappings");
            
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index 'stores'", e);
            // 인덱스 생성 실패가 애플리케이션 시작을 막지 않도록 예외를 던지지 않음
        }
    }
}

