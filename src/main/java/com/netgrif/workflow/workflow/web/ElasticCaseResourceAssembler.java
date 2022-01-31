package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.workflow.web.responsebodies.ElasticCaseResource;
import org.springframework.hateoas.ResourceAssembler;

public class ElasticCaseResourceAssembler implements ResourceAssembler<ElasticCase, ElasticCaseResource>  {

    @Override
    public ElasticCaseResource toResource(ElasticCase entity) {
        return new ElasticCaseResource(entity);
    }
}