package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.domain.store.document.StoreDocument;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.repository.StoreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 매장 정보와 Elasticsearch 간의 동기화를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreElasticsearchSyncService {

    private final StoreSearchRepository storeSearchRepository;

    /**
     * 매장 생성 시 Elasticsearch에 문서 저장
     */
    public void indexStore(Store store) {
        try {
            StoreDocument document = StoreDocument.from(store);
            storeSearchRepository.save(document);
            log.info("Successfully indexed store to Elasticsearch: storeId={}", store.getId());
        } catch (Exception e) {
            log.error("Failed to index store to Elasticsearch: storeId={}", store.getId(), e);
            // ES 동기화 실패가 비즈니스 로직을 막지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 매장 수정 시 Elasticsearch 문서 업데이트
     */
    public void updateStore(Store store) {
        try {
            StoreDocument document = StoreDocument.from(store);
            storeSearchRepository.save(document);
            log.info("Successfully updated store in Elasticsearch: storeId={}", store.getId());
        } catch (Exception e) {
            log.error("Failed to update store in Elasticsearch: storeId={}", store.getId(), e);
        }
    }

    /**
     * 매장 삭제 시 Elasticsearch 문서 제거
     */
    public void deleteStore(Long storeId) {
        try {
            storeSearchRepository.deleteByStoreId(storeId);
            log.info("Successfully deleted store from Elasticsearch: storeId={}", storeId);
        } catch (Exception e) {
            log.error("Failed to delete store from Elasticsearch: storeId={}", storeId, e);
        }
    }
}

