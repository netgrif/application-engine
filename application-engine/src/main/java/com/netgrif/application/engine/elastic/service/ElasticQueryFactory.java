package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticQueryFactory;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ElasticQueryFactory implements IElasticQueryFactory {

    private final SimpleTemplateEngine templateEngine;
    @Getter
    private final Map<String, Object> context;

    public ElasticQueryFactory() {
        this.templateEngine = new SimpleTemplateEngine();
        this.context = new HashMap<>();
    }

    @Override
    public String populateQuery(String query, Map<String, Object> queryContext) {
        String populatedQuery = query;
        try {
            queryContext.putAll(context);
            Template template = templateEngine.createTemplate(query);
            populatedQuery = template.make(queryContext).toString();
        } catch (CompilationFailedException | ClassNotFoundException | IOException e) {
            log.error("Cannot populate template from string query", e);
        }
        return populatedQuery;
    }

}
