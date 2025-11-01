package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.document.StoreDocument;

/**
 * 매장 자동완성 제안 응답 DTO
 * 검색창에서 실시간 자동완성에 사용
 */
public record StoreSuggestResponse(
        Long id,
        String name
) {

    public static StoreSuggestResponse from(StoreDocument document) {
        return new StoreSuggestResponse(
                document.getStoreId(),
                document.getName()
        );
    }
}

