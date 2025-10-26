package com.sparta.couponpop.domain.store.document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String address;

    @Field(type = FieldType.Text)
    private String dong;

    @Field(type = FieldType.Text)
    private String storeCategory;

    @Field(type = FieldType.Double)
    private Double latitude;

    @Field(type = FieldType.Double)
    private Double longitude;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime deletedAt;

    @Builder
    private StoreDocument(String id, String name, String description, String address, String dong,
                         String storeCategory, Double latitude, Double longitude, String imageUrl,
                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.dong = dong;
        this.storeCategory = storeCategory;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static StoreDocument from(Long id, String name, String description, String address, String dong,
                                    String storeCategory, Double latitude, Double longitude, String imageUrl,
                                    LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return StoreDocument.builder()
                .id(String.valueOf(id))
                .name(name)
                .description(description)
                .address(address)
                .dong(dong)
                .storeCategory(storeCategory)
                .latitude(latitude)
                .longitude(longitude)
                .imageUrl(imageUrl)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();
    }
}
