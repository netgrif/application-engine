package com.netgrif.workflow.elastic.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.elastic.domain.*;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ElasticCaseMappingService implements IElasticCaseMappingService {
    @Override
    public ElasticCase transform(Case useCase) {
        ElasticCase transformedCase = new ElasticCase(useCase);
        this.populateDataSet(transformedCase, useCase);
        return transformedCase;
    }

    protected void populateDataSet(ElasticCase transformedCase, Case useCase) {
        for (String id : useCase.getImmediateDataFields()) {
            Optional<DataField> parsedValue = this.transformDataField(useCase.getDataField(id));
            parsedValue.ifPresent(dataField -> transformedCase.getDataSet().put(id, dataField));
        }
    }

    protected Optional<DataField> transformDataField(com.netgrif.workflow.workflow.domain.DataField dataField) {
        // Set<I18nString>
        if (dataField.getOptions() != null) {
            if (dataField.getValue() instanceof Set) {
                return this.transformMultichoiceMapField(dataField);
            } else {
                return this.transformEnumerationMapField(dataField);
            }
        } else if (dataField.getValue() instanceof Set) {
            if (dataField.getValue() == null)
                return Optional.empty();
            return this.transformMultichoiceField(dataField);
        } else if (dataField.getValue() instanceof Number) {
            return this.transformNumberField(dataField);
        } else if (dataField.getValue() instanceof User) {
            return this.transformUserField(dataField);
        } else if (dataField.getValue() instanceof LocalDate) {
            LocalDate date = (LocalDate) dataField.getValue();
            return transformDateField(LocalDateTime.of(date, LocalTime.NOON));
        } else if (dataField.getValue() instanceof LocalDateTime) {
            return transformDateField((LocalDateTime) dataField.getValue());
        } else if (dataField.getValue() instanceof Date) {
            LocalDateTime date = ((Date) dataField.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return transformDateField(date);
        } else if (dataField.getValue() instanceof Boolean) {
            return Optional.of(new BooleanField((Boolean) dataField.getValue()));
        } else {
            if (dataField.getValue() == null)
                return Optional.empty();
            String string = dataField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return Optional.of(new TextField(string));
        }
    }

    protected Optional<DataField> transformMultichoiceMapField(com.netgrif.workflow.workflow.domain.DataField multichoiceMap) {
        List<Map.Entry<String, I18nString>> values = new ArrayList<>();
        for(String key : (Set<String>) multichoiceMap.getValue()) {
            values.add(new AbstractMap.SimpleEntry<>(key, multichoiceMap.getOptions().get(key)));
        }
        return Optional.of(new MapField(values));
    }

    protected Optional<DataField> transformEnumerationMapField(com.netgrif.workflow.workflow.domain.DataField enumMap) {
        String selectedKey = (String) enumMap.getValue();
        return Optional.of(
                new MapField(
                        new AbstractMap.SimpleEntry<>(selectedKey, enumMap.getOptions().get(selectedKey))
                )
        );
    }

    protected Optional<DataField> transformMultichoiceField(com.netgrif.workflow.workflow.domain.DataField multichoiceField) {
        Set values = (Set) multichoiceField.getValue();
        return Optional.of(new TextField((String[]) values.stream().map(Object::toString).toArray(String[]::new)));
    }

    protected Optional<DataField> transformNumberField(com.netgrif.workflow.workflow.domain.DataField numberField) {
        return Optional.of(new NumberField((Double) numberField.getValue()));
    }

    protected Optional<DataField> transformUserField(com.netgrif.workflow.workflow.domain.DataField userField) {
        User user = (User) userField.getValue();
        if (user == null)
            return Optional.empty();
        StringBuilder fullName = new StringBuilder();
        if (user.getSurname() != null) {
            fullName.append(user.getSurname());
            fullName.append(" ");
        }
        if (user.getName() != null) {
            fullName.append(user.getName());
        }
        return Optional.of(new UserField(user.getId(), user.getEmail(), fullName.toString()));
    }

    private Optional<DataField> transformDateField(LocalDateTime date) {
        if (date == null)
            return Optional.empty();
        return Optional.of(new DateField(date.format(DateTimeFormatter.BASIC_ISO_DATE), date));
    }
}
