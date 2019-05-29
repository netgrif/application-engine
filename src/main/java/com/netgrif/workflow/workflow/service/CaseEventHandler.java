package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.IElasticCaseService;
import com.netgrif.workflow.workflow.domain.Case;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private IElasticCaseService service;

    @Override
    public void onAfterSave(AfterSaveEvent<Case> event) {
        service.indexNow(new ElasticCase(event.getSource()));
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }
        ObjectId objectId = document.getObjectId("_id");
        service.remove(objectId.toString());
    }
}