package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticCasePrioritySearch implements IElasticCasePrioritySearch {


    @Autowired
    protected DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;

    @Override
    public List<String> fullTextFields() {
        return elasticsearchProperties.getPriority().getFullTextFields();
    }
}
