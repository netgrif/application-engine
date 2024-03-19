package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private IElasticCaseService service;

    @Autowired
    private CaseRepository repository;

    private final Map<String, String> caseUriNodes;


    public CaseEventHandler() {
        this.caseUriNodes =  new ConcurrentHashMap<>();;
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Case> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }

        ObjectId objectId = document.getObjectId("_id");
        if (objectId != null) {
            Case useCase = repository.findById(event.getDocument().getObjectId("_id").toString()).get();
            caseUriNodes.put(useCase.getStringId(), useCase.getUriNodeId());
        }
    }


    @Override
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }

        ObjectId objectId = document.getObjectId("_id");
        if (objectId != null) {
            service.remove(objectId.toString(), caseUriNodes.get(objectId.toString()));
            caseUriNodes.remove(objectId.toString());
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