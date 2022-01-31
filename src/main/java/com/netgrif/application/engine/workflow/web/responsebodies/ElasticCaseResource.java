package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class ElasticCaseResource extends EntityModel<ElasticCase> {

    public ElasticCaseResource(ElasticCase content) {
        super(content, new ArrayList<>());
    }
}