package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.workflow.domain.Task;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskEventHandler extends AbstractMongoEventListener<Task> {

    private static final Logger log = LoggerFactory.getLogger(TaskEventHandler.class);

    @Autowired
    private IElasticTaskService service;

    @Autowired
    private IElasticTaskMappingService taskMappingService;

    @Async
    @Override
    public void onAfterSave(AfterSaveEvent<Task> event) {
        service.index(this.taskMappingService.transform(event.getSource()));
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Task> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }

        ObjectId objectId = document.getObjectId("_id");
        if (objectId != null) {
            service.remove(objectId.toString());
            return;
        }

        String processId = document.getString("processId");
        if (processId != null) {
            service.removeByPetriNetId(processId);
            return;
        }

        throw new IllegalStateException("Task has been deleted neither by ID nor by process ID!");
    }
}