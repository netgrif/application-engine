package com.netgrif.workflow.integration.dashboards.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.workflow.configuration.properties.ElasticsearchProperties;
import com.netgrif.workflow.elastic.service.ElasticCaseService;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.integration.dashboards.service.interfaces.IDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.InternalOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class DashboardService implements IDashboardService {

    @Autowired
    private ElasticsearchProperties properties;

    @Override
    public String searchByQuery(String searchBody, String type) {
        HttpHeaders headers = makeHeaders();
        String elasticSearcHost = "http://" + properties.getUrl() + ":" + properties.getSearchPort() + "/" + properties.getIndex().get(type) + "/_search";
        String responseBody;
        HttpEntity entity = new HttpEntity<>(searchBody, headers);
        RestTemplate elasticRestTemplate = new RestTemplate();
        ResponseEntity<String> response = elasticRestTemplate.exchange(
                    elasticSearcHost, HttpMethod.POST, entity, String.class);
        responseBody = response.getBody();
        return "{" + responseBody.substring(responseBody.indexOf("\"aggregations"));
    }

    private HttpHeaders makeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
