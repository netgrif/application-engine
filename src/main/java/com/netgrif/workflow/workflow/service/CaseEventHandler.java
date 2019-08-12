package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

@Component
public class CaseEventHandler extends AbstractMongoEventListener<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private IElasticCaseService service;

    @Autowired
    private FieldFactory fieldFactory;

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public void onAfterSave(AfterSaveEvent<Case> event) {
        Case useCase = event.getSource();
        setImmediateData(useCase);
        service.indexNow(new ElasticCase(useCase));
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

    private void setImmediateData(Case useCase) {
        if (useCase.getPetriNet() == null) {
            setPetriNet(useCase);
        }
        List<Field> immediateData = new ArrayList<>();

        useCase.getImmediateDataFields().forEach(fieldId ->
                immediateData.add(fieldFactory.buildImmediateField(useCase, fieldId))
        );
        LongStream.range(0L, immediateData.size()).forEach(index -> immediateData.get((int) index).setOrder(index));

        useCase.setImmediateData(immediateData);
    }

    private void setPetriNet(Case useCase) {
        PetriNet model = petriNetService.clone(useCase.getPetriNetObjectId());
        model.initializeTokens(useCase.getActivePlaces());
        model.initializeVarArcs(useCase.getDataSet());
        useCase.setPetriNet(model);
    }
}