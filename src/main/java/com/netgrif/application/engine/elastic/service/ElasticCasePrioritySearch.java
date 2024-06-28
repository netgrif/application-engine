package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.configuration.ElasticCaseSearchConfiguration;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticCasePrioritySearch implements IElasticCasePrioritySearch {


    @Autowired
    protected ElasticCaseSearchConfiguration elasticCaseSearchConfiguration;

    @Override
    public List<String> fullTextFields() {
        return elasticCaseSearchConfiguration.getFullTextFields();
    }
}
