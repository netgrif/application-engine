package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.configuration.ElasticCaseSearchConfiguration;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ElasticCasePrioritySearch implements IElasticCasePrioritySearch {


    @Autowired
    protected ElasticCaseSearchConfiguration elasticCaseSearchConfiguration;

    @Override
    public Map<String, Float> fullTextFields() {
        return elasticCaseSearchConfiguration.getFullTextFieldMap();
    }
}
