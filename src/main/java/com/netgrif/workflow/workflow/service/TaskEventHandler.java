package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.elastic.service.IElasticTaskService;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventHandler extends AbstractMongoEventListener<Task> {

    @Autowired
    private IElasticTaskService service;

    @Async
    @Override
    public void onAfterSave(AfterSaveEvent<Task> event) {
        service.index(event.getSource());
    }
}