package com.netgrif.application.engine.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.elastic.search.priority")
public class ElasticCaseSearchConfiguration {

    private List<String> fullTextFields = List.of(
            "title.keyword^2",
            "authorName^1",
            "authorEmail^1",
            "visualId.keyword^2"
    );

}
