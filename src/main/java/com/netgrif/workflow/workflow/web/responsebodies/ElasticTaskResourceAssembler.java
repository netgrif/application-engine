package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import org.springframework.hateoas.ResourceAssembler;

public class ElasticTaskResourceAssembler implements ResourceAssembler<ElasticTask, LocalisedTaskResource> {

    @Override
    public LocalisedTaskResource toResource(ElasticTask entity) {
        return new LocalisedTaskResource(new Task(entity));
    }
}