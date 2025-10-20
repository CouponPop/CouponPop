package com.sparta.couponpop.domain.store.dto.response;

import com.sparta.couponpop.domain.store.enums.StoreCategory;

/**
 * 위치 기반 매장 조회를 위한 인터페이스 프로젝션
 * Native Query 결과를 매핑하기 위해 사용됩니다.
 */
public interface StoreLocationProjection {
	Long getId();
	String getName();
	String getAddress();
	String getStoreCategory(); // Enum이 아닌 String으로 받음
	Double getLatitude();
	Double getLongitude();
	String getImageUrl();
	Double getDistance();

	/**
	 * String을 StoreCategory Enum으로 변환하여 StoreMapResponse를 생성합니다.
	 */
	default StoreMapResponse toStoreMapResponse() {
		return new StoreMapResponse(
			getId(),
			getName(),
			getAddress(),
			StoreCategory.valueOf(getStoreCategory()), // String을 Enum으로 변환
			getLatitude(),
			getLongitude(),
			getImageUrl(),
			getDistance()
		);
	}
}