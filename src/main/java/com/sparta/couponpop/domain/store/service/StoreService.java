package com.sparta.couponpop.domain.store.service;

import com.sparta.couponpop.common.exception.GlobalException;
import com.sparta.couponpop.domain.member.entity.Member;
import com.sparta.couponpop.domain.member.exception.MemberErrorCode;
import com.sparta.couponpop.domain.member.repository.MemberRepository;
import com.sparta.couponpop.domain.store.dto.request.CreateStoreRequest;
import com.sparta.couponpop.domain.store.dto.response.StoreDetailResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreLocationProjection;
import com.sparta.couponpop.domain.store.dto.response.StoreMapResponse;
import com.sparta.couponpop.domain.store.dto.response.StoreResponse;
import com.sparta.couponpop.domain.store.entity.Store;
import com.sparta.couponpop.domain.store.exception.StoreErrorCode;
import com.sparta.couponpop.domain.store.repository.StoreRepository;
import com.sparta.couponpop.domain.store.document.StoreDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final MemberRepository memberRepository;

    @Transactional
    public StoreResponse createStore(Long memberId, CreateStoreRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));

        Store store = Store.createStore(
                member,
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );

        Store savedStore = storeRepository.save(store);

        return StoreResponse.from(savedStore);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, Long memberId, CreateStoreRequest request) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 매장 소유자 검증
        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_UPDATE_PERMISSION_DENIED);
        }

        store.updateStoreInfo(
                request.name(),
                request.phone(),
                request.description(),
                request.businessNumber(),
                request.address(),
                request.dong(),
                request.latitude(),
                request.longitude(),
                request.imageUrl(),
                request.storeCategory(),
                request.weekdayOpenTime(),
                request.weekdayCloseTime(),
                request.weekendOpenTime(),
                request.weekendCloseTime()
        );

        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getStoresByOwner(Long memberId) {

        List<Store> stores = storeRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return stores.stream()
                .map(StoreResponse::from)
                .toList();
    }

    @Transactional
    public void deleteStore(Long storeId, Long memberId) {

        Store store = storeRepository.findByIdIncludingDeleted(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        // 이미 삭제된 매장인지 확인
        if (store.getDeletedAt() != null) {
            throw new GlobalException(StoreErrorCode.STORE_ALREADY_DELETED);
        }

        // 매장 소유자 검증
        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_DELETE_PERMISSION_DENIED);
        }

        store.deleteStore();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(Long storeId, Long memberId) {

        Store store = storeRepository.findByIdWithMember(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getId().equals(memberId)) {
            throw new GlobalException(StoreErrorCode.STORE_ACCESS_PERMISSION_DENIED);
        }

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetailForCustomer(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(StoreErrorCode.STORE_NOT_FOUND));

        return StoreDetailResponse.from(store);
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> searchStoresByName(String keyword) {

        try {
            // Criteria를 사용하여 안전한 쿼리 생성 (JSON Injection 방지)
            Criteria criteria = new Criteria("name").contains(keyword)
                    .or(new Criteria("description").contains(keyword))
                    .or(new Criteria("address").contains(keyword))
                    .and(new Criteria("deleted_at").is(null)); // 삭제되지 않은 매장만
            
            Query query = new CriteriaQuery(criteria);
            
            SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);

            // Elasticsearch 결과에서 ID 추출
            List<Long> storeIds = searchHits.getSearchHits().stream()
                    .map(SearchHit::getId)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (storeIds.isEmpty()) {
                log.debug("[Elasticsearch] 검색 결과 없음: keyword={}", keyword);
                return List.of();
            }

            // MySQL에서 실제 데이터 조회
            List<Store> stores = storeRepository.findAllById(storeIds);

            log.debug("[Elasticsearch] 검색 완료: keyword={}, count={}", keyword, stores.size());

            return stores.stream()
                    .map(StoreResponse::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[Elasticsearch] 검색 실패, MySQL 폴백: keyword={}, error={}", keyword, e.getMessage());
            
            // Elasticsearch 실패 시 MySQL 폴백
            List<Store> stores = storeRepository.findByNameContainingIgnoreCase(keyword);

            return stores.stream()
                    .map(StoreResponse::from)
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public List<StoreMapResponse> getStoresByLocation(double latitude, double longitude, double radiusKm) {

        return storeRepository.findByLocation(latitude, longitude, radiusKm)
            .stream()
            .map(StoreLocationProjection::toStoreMapResponse)
            .toList();
    }
}
