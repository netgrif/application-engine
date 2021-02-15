package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.workflow.web.responsebodies.ElasticCaseResource;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ElasticCaseResourceAssembler implements RepresentationModelAssembler<ElasticCase, ElasticCaseResource>  {

    @Override
    public ElasticCaseResource toModel(ElasticCase entity) {
        return new ElasticCaseResource(entity);
    }
}