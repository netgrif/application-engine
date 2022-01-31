package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.workflow.web.responsebodies.ElasticCaseResource;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ElasticCaseResourceAssembler implements RepresentationModelAssembler<ElasticCase, ElasticCaseResource> {

    @Override
    public ElasticCaseResource toModel(ElasticCase entity) {
        return new ElasticCaseResource(entity);
    }
}