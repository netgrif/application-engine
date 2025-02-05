package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.core.workflow.domain.Case;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private IElasticCaseService service;

    @Override
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }

        String objectId = ((Document)document.get("_id")).get("shortProcessId") + "-" + ((Document)document.get("_id")).get("objectId").toString();
        if (objectId != null) {
            service.remove(objectId);
            return;
        }

        objectId = document.getObjectId("petriNetObjectId").toString();
        if (objectId != null) {
            service.removeByPetriNetId(objectId);
            return;
        }

        throw new IllegalStateException("Case has been deleted neither by ID nor by process ID!");
    }
}