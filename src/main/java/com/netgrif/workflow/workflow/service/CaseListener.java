package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CaseListener extends AbstractMongoEventListener<Case> {

    @Autowired
    private StandardPBEStringEncryptor encryptor;

    @Override
    public void onAfterConvert(AfterConvertEvent<Case> event) {
        List<DataField> dataFields = getDataFields(event);

        dataFields.forEach(field -> field.setValue(encryptor.decrypt((String) field.getValue())));

        super.onAfterConvert(event);
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Case> event) {
        List<DataField> dataFields = getDataFields(event);

        dataFields.forEach(field -> field.setValue(encryptor.encrypt((String) field.getValue())));

        super.onBeforeSave(event);
    }

    private List<DataField> getDataFields(MongoMappingEvent<Case> event) {
        Case useCase = event.getSource();
        PetriNet net = useCase.getPetriNet();
        List<String> fields = net.getDataSet().values().stream()
                .filter(field -> "Rodné číslo".equalsIgnoreCase(field.getName()))
                .map(Field::getStringId)
                .collect(Collectors.toList());

        return useCase.getDataSet().entrySet().stream()
                .filter(e -> fields.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}