package com.netgrif.workflow.workflow.domain.elastic;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    @Autowired
    private IElasticCaseService service;

    @Async
    @Override
    public void onAfterSave(AfterSaveEvent<Case> event) {
        service.index(event.getSource());
    }
}