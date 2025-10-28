package com.sparta.couponpop.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    /**
     * Low Level REST Client - REST API를 직접 사용
     * 예: Request request = new Request("GET", "/index/_search");
     */
    @Bean
    public RestClient restClient() {
        URI uri = URI.create(elasticsearchUri);
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        
        return RestClient.builder(host).build();
    }

    /**
     * High Level Client - 타입 세이프한 클라이언트
     * 예: esClient.search(s -> s.index("index").query(q -> q.match(m -> m.field("field").query("value"))))
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
        );

        return new ElasticsearchClient(transport);
    }
}

