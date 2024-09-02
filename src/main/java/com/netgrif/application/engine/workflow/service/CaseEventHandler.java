package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.workflow.domain.Case;
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
public class CaseEventHandler {

    private final IElasticCaseService service;
    private final IElasticCaseMappingService caseMappingService;

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'case'")
    public void onAfterSave(AfterSaveEvent<Case> event) {
        Case useCase = event.getSource();
        try {
            useCase.resolveImmediateDataFields();
            service.indexNow(caseMappingService.transform(useCase));
        } catch (Exception e) {
            log.error("Indexing failed [{}]", useCase.getStringId(), e);
        }
    }

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'case'")
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
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

        objectId = document.getObjectId("petriNetObjectId");
        if (objectId != null) {
            service.removeByPetriNetId(objectId.toString());
            return;
        }

        throw new IllegalStateException("Case has been deleted neither by ID nor by process ID!");
    }
}