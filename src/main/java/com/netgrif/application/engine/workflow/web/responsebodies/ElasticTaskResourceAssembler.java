package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ElasticTaskResourceAssembler implements RepresentationModelAssembler<ElasticTask, TaskResource> {

    @Override
    public TaskResource toModel(ElasticTask entity) {
        return new TaskResource(new Task(entity));
    }
}