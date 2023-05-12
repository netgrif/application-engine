package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

@Component
public class PetriNetEventHandler extends AbstractMongoEventListener<PetriNet> {

    private static final Logger log = LoggerFactory.getLogger(PetriNetEventHandler.class);

    @Autowired
    private IElasticPetriNetService service;

    @Override
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
