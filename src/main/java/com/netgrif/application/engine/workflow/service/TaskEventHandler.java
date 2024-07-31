package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventHandler {

    private final IElasticTaskService service;
    private final IElasticTaskMappingService taskMappingService;

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'task'")
    public void onAfterSave(AfterSaveEvent<Task> event) {
        service.index(this.taskMappingService.transform(event.getSource()));
    }

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'task'")
    public void onAfterDelete(AfterDeleteEvent<Task> event) {
        Document document = event.getDocument();
        if (document == null || document.isEmpty()) {
            log.warn("Trying to delete null or empty document!");
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