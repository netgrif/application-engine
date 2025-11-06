package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private IElasticCaseService service;

    @Autowired
    private FieldFactory fieldFactory;

    @Autowired
    private IWorkflowService workflowService;

    @Override
    public void onAfterConvert(AfterConvertEvent<Case> event) {
        Case useCase = event.getSource();
        workflowService.setPetriNet(useCase);
        List<Field<?>> immediateFields = new ArrayList<>();
        if (useCase.getImmediateDataFields() != null) {
            useCase.getImmediateDataFields().forEach(fieldId -> {
                try {
                    immediateFields.add(fieldFactory.buildImmediateField(useCase, fieldId));
                } catch (Exception e) {
                    log.error("Could not build immediate field for case {} and field {}", useCase.getStringId(), fieldId, e);
                }
            }
            );
            useCase.setImmediateData(immediateFields);
        }
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Case> event) {
        Document document = event.getDocument();
        if (document == null) {
            log.warn("Trying to delete null document!");
            return;
        }

        String objectId = ((Document)document.get("_id")).get("shortProcessId") + "-" + ((Document)document.get("_id")).get("objectId").toString();
        service.remove(objectId);
    }
}
