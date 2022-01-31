package com.netgrif.application.engine.integration.dashboards.service;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.integration.dashboards.service.interfaces.IDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
