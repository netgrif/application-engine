package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.workflow.domain.Task;
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

    @Async
    @Override
    public void onAfterSave(AfterSaveEvent<Task> event) {
        service.index(new ElasticTask(event.getSource()));
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Task> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }
        ObjectId objectId = document.getObjectId("_id");
        service.remove(objectId.toString());
    }
}