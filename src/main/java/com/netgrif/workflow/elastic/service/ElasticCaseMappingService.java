package com.netgrif.workflow.elastic.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.elastic.domain.*;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.petrinet.domain.dataset.FileListFieldValue;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
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
            return this.transformDateField(dataField);
        } else if (dataField.getValue() instanceof LocalDateTime) {
            return this.transformDateTimeField(dataField);
        } else if (dataField.getValue() instanceof Date) {
            log.warn("DataField with Date instance value was found! DateFields should use LocalDate instances, DateTimeFields should use LocalDateTime instances! Converting the value to a LocalDateTime instance...");
            LocalDateTime date = ((Date) dataField.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return this.formatDateField(date);
        } else if (dataField.getValue() instanceof Boolean) {
            return this.transformBooleanField(dataField);
        } else if (dataField.getValue() instanceof I18nString) {
            return this.transformEnumerationField(dataField);
        } else if (dataField.getValue() instanceof String) {
            return this.transformTextField(dataField);
        } else if (dataField.getValue() instanceof FileFieldValue) {
            return this.transformFileField(dataField);
        } else if (dataField.getValue() instanceof FileListFieldValue) {
            return this.transformFileListField(dataField);
        } else {
            if (dataField.getValue() == null)
                return Optional.empty();
            String string = dataField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return this.transformOtherFields(dataField);
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
        List<String> translations = new ArrayList<>();
        values.forEach(i18n -> translations.addAll(this.collectTranslations((I18nString) i18n)));
        return Optional.of(new TextField(translations.toArray(new String[0])));
    }

    protected Optional<DataField> transformEnumerationField(com.netgrif.workflow.workflow.domain.DataField enumField) {
        return Optional.of(new TextField(this.collectTranslations((I18nString) enumField.getValue()).toArray(new String[0])));
    }

    protected List<String> collectTranslations(I18nString i18nString) {
        List<String> translations = new ArrayList<>();
        translations.add(i18nString.getDefaultValue());
        translations.addAll(i18nString.getTranslations().values());
        return translations;
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

    protected Optional<DataField> transformDateField(com.netgrif.workflow.workflow.domain.DataField dateField) {
        LocalDate date = (LocalDate) dateField.getValue();
        return formatDateField(LocalDateTime.of(date, LocalTime.NOON));
    }

    protected Optional<DataField> transformDateTimeField(com.netgrif.workflow.workflow.domain.DataField dateTimeField) {
        return formatDateField((LocalDateTime) dateTimeField.getValue());
    }

    private Optional<DataField> formatDateField(LocalDateTime date) {
        if (date == null)
            return Optional.empty();
        return Optional.of(new DateField(date.format(DateTimeFormatter.BASIC_ISO_DATE), date));
    }

    protected Optional<DataField> transformBooleanField(com.netgrif.workflow.workflow.domain.DataField booleanField) {
        return Optional.of(new BooleanField((Boolean) booleanField.getValue()));
    }

    protected Optional<DataField> transformTextField(com.netgrif.workflow.workflow.domain.DataField textField) {
        if (textField.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(new TextField((String) textField.getValue()));
    }

    protected Optional<DataField> transformFileField(com.netgrif.workflow.workflow.domain.DataField fileField) {
        return Optional.of(new FileField((FileFieldValue) fileField.getValue()));
    }

    protected Optional<DataField> transformFileListField(com.netgrif.workflow.workflow.domain.DataField fileListField) {
        return Optional.of(new FileField(((FileListFieldValue) fileListField.getValue()).getNamesPaths().toArray(new FileFieldValue[0])));
    }

    protected Optional<DataField> transformOtherFields(com.netgrif.workflow.workflow.domain.DataField otherField) {
        log.warn("Fields with value of type " + otherField.getValue().getClass().getCanonicalName() + " is not supported for indexation by default. Indexing the toString() representation...");
        return Optional.of(new TextField(otherField.getValue().toString()));
    }
}
