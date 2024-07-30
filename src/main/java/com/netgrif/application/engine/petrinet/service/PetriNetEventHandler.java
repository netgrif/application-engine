package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
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
public class PetriNetEventHandler {

    private final IElasticPetriNetService service;
    private final IElasticPetriNetMappingService petriNetMappingService;

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'petriNet'")
    public void onAfterSave(AfterSaveEvent<PetriNet> event) {
        PetriNet net = event.getSource();
        try {
            service.indexNow(petriNetMappingService.transform(net));
        } catch (Exception e) {
            log.error("Indexing failed [{}]", net.getStringId(), e);
        }
    }

    @TransactionalEventListener(fallbackExecution = true, condition = "#event.collectionName == 'petriNet'")
    public void onAfterDelete(AfterDeleteEvent<PetriNet> event) {
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

        throw new IllegalStateException("PetriNet hasn't been deleted by ID!");
    }
}
