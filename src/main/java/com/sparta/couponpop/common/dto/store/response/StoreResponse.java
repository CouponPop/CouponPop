package com.sparta.couponpop.common.dto.store.response;

public record StoreResponse(
        Long id,
        String name
) {
    public static StoreResponse of(Long id, String name) {
        return new StoreResponse(id, name);
    }
}
