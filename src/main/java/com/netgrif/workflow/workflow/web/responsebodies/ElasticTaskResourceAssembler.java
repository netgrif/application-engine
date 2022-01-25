package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ElasticTaskResourceAssembler implements RepresentationModelAssembler<ElasticTask, LocalisedTaskResource> {

    @Override
    public LocalisedTaskResource toModel(ElasticTask entity) {
        return new LocalisedTaskResource(new Task(entity));
    }
}