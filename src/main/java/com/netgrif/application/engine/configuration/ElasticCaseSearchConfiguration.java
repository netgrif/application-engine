package com.netgrif.application.engine.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.elastic.search.priority")
public class ElasticCaseSearchConfiguration {

    private Map<String, Float> fullTextFieldMap = Map.of(
            "title.keyword", 2f,
            "authorName", 1f,
            "authorEmail", 1f,
            "visualId.keyword", 2f
    );

}
