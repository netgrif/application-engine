package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    @Autowired
    private IElasticCaseService service;

    @Override
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
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

        objectId = document.getObjectId("petriNetObjectId");
        if (objectId != null) {
            service.removeByPetriNetId(objectId.toString());
            return;
        }

        throw new IllegalStateException("Case has been deleted neither by ID nor by process ID!");
    }
}