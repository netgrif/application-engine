package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class ElasticCaseResource extends Resource<ElasticCase> {

    public ElasticCaseResource(ElasticCase content) {
        super(content, new ArrayList<>());
    }
}