package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Task;
import org.bson.types.ObjectId;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ElasticTaskResourceAssembler implements RepresentationModelAssembler<ElasticTask, TaskResource> {

    @Override
    public TaskResource toModel(ElasticTask entity) {
        Task taskModel = new Task();
        Task.with()
                ._id(new ObjectId(entity.getStringId()))
                .caseId(entity.getCaseId())
                .transitionId(entity.getTransitionId())
                .title(new I18nString(entity.getTitle()))
                .caseTitle(entity.getCaseTitle())
                .priority(entity.getPriority())
                .build();
        return new TaskResource(taskModel);
    }
}