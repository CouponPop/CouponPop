package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.domain.store.document.StoreDocument;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 인덱스 초기화 및 재색인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreIndexInitService {

    private final StoreRepository storeRepository;
    private final StoreSearchRepository storeSearchRepository;

    /**
     * 모든 매장 데이터를 Elasticsearch에 재색인
     * 기존 데이터가 있는 경우 사용
     */
    @Transactional(readOnly = true)
    public void reindexAllStores() {
        log.info("Starting reindexing all stores to Elasticsearch...");
        
        try {
            // 모든 활성 매장 조회 (soft delete 제외)
            List<Store> allStores = storeRepository.findAll();
            log.info("Found {} stores to reindex", allStores.size());

            // StoreDocument로 변환
            List<StoreDocument> documents = allStores.stream()
                    .map(StoreDocument::from)
                    .collect(Collectors.toList());

            // 배치로 저장
            storeSearchRepository.saveAll(documents);
            
            log.info("Successfully reindexed {} stores to Elasticsearch", documents.size());
        } catch (Exception e) {
            log.error("Failed to reindex stores to Elasticsearch", e);
            throw new RuntimeException("Reindexing failed", e);
        }
    }

    /**
     * Elasticsearch 인덱스 삭제 (개발/테스트 용도)
     */
    public void deleteAllStoresFromIndex() {
        log.warn("Deleting all stores from Elasticsearch index...");
        try {
            storeSearchRepository.deleteAll();
            log.info("Successfully deleted all stores from Elasticsearch index");
        } catch (Exception e) {
            log.error("Failed to delete stores from Elasticsearch index", e);
            throw new RuntimeException("Index deletion failed", e);
        }
    }

    /**
     * 전체 재색인 (기존 인덱스 삭제 후 재생성)
     */
    @Transactional(readOnly = true)
    public void fullReindex() {
        log.info("Starting full reindex (delete and recreate)...");
        deleteAllStoresFromIndex();
        reindexAllStores();
        log.info("Full reindex completed");
    }
}

