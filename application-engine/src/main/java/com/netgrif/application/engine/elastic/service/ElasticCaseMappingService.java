package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.objects.elastic.domain.*;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.TaskField;
import com.netgrif.application.engine.objects.workflow.domain.Case;
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
        ElasticCase transformedCase = new com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase(useCase);
        this.populateDataSet(transformedCase, useCase);
        return transformedCase;
    }

    protected void populateDataSet(ElasticCase transformedCase, Case useCase) {
        for (String id : useCase.getImmediateDataFields()) {
            Optional<DataField> parsedValue = this.transformDataField(id, useCase);
            parsedValue.ifPresent(dataField -> transformedCase.getDataSet().put(id, dataField));
        }
    }

    protected Optional<DataField> transformDataField(String fieldId, Case useCase) {
        Field<?> netField = useCase.getField(fieldId);
        com.netgrif.application.engine.objects.workflow.domain.DataField caseField = useCase.getDataField(fieldId);

        if (caseField.getValue() == null) {
            return Optional.empty();
        } else if (netField instanceof EnumerationMapField) {
            return this.transformEnumerationMapField(caseField, (EnumerationMapField) netField);
        } else if (netField instanceof MultichoiceMapField) {
            return this.transformMultichoiceMapField(caseField, (MultichoiceMapField) netField);
        } else if (netField instanceof MultichoiceField) {
            return this.transformMultichoiceField(caseField, (MultichoiceField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.NumberField) {
            return this.transformNumberField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.ButtonField) {
            return this.transformButtonField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorField) {
            return this.transformActorField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.DateField) {
            return this.transformDateField(caseField, (com.netgrif.application.engine.objects.petrinet.domain.dataset.DateField) netField);
        } else if (netField instanceof DateTimeField) {
            return this.transformDateTimeField(caseField, (DateTimeField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.BooleanField) {
            return this.transformBooleanField(caseField);
        } else if (netField instanceof EnumerationField) {
            return this.transformEnumerationField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.TextField) {
            return this.transformTextField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.FileField) {
            return this.transformFileField(caseField);
        } else if (netField instanceof FileListField) {
            return this.transformFileListField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorListField) {
            return this.transformActorListField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.I18nField) {
            return this.transformI18nField(caseField, (com.netgrif.application.engine.objects.petrinet.domain.dataset.I18nField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.CaseField) {
            return this.transformCaseFieldField(caseField, (com.netgrif.application.engine.objects.petrinet.domain.dataset.CaseField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.FilterField) {
            return this.transformFilterFieldField(caseField, (com.netgrif.application.engine.objects.petrinet.domain.dataset.FilterField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.StringCollectionField) {
            return this.transformStringCollectionField(caseField, (com.netgrif.application.engine.objects.petrinet.domain.dataset.StringCollectionField) netField);
        } else if (netField instanceof com.netgrif.application.engine.objects.petrinet.domain.dataset.TaskField) {
            return this.transformTaskField(caseField);
        } else {
            String string = caseField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return this.transformOtherFields(caseField, netField);
        }
    }

    protected Optional<DataField> transformMultichoiceMapField
            (com.netgrif.application.engine.objects.workflow.domain.DataField multichoiceMap, MultichoiceMapField netField) {
        Optional<Set> optValues = this.getMultichoiceValue(multichoiceMap, netField);
        if (optValues.isEmpty()) {
            return Optional.empty();
        }
        Set mapValues = optValues.get();
        Map<String, I18nString> options = this.getFieldOptions(multichoiceMap, netField);
        List<Map.Entry<String, I18nString>> values = new ArrayList<>();
        for (String key : (Set<String>) mapValues) {
            I18nString selectedValue = options.get(key);
            values.add(new AbstractMap.SimpleEntry<>(key, selectedValue != null ? selectedValue : new I18nString("")));
        }
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.MapField(values));
    }

    protected Optional<DataField> transformI18nField
            (com.netgrif.application.engine.objects.workflow.domain.DataField
                     dataField, com.netgrif.application.engine.objects.petrinet.domain.dataset.I18nField netField) {
        Set<String> keys = ((I18nString) dataField.getValue()).getTranslations().keySet();
        Set<String> values = new HashSet<>(((I18nString) dataField.getValue()).getTranslations().values());
        values.add(((I18nString) dataField.getValue()).getDefaultValue());
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.I18nField(keys, values, ((I18nString) dataField.getValue()).getTranslations()));
    }

    protected Optional<DataField> transformCaseFieldField(com.netgrif.application.engine.objects.workflow.domain.DataField dataField,
                                                          com.netgrif.application.engine.objects.petrinet.domain.dataset.CaseField netField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.CaseField(
                (List<String>) dataField.getValue(), dataField.getAllowedNets()));
    }

    protected Optional<DataField> transformFilterFieldField(com.netgrif.application.engine.objects.workflow.domain.DataField dataField,
                                                            com.netgrif.application.engine.objects.petrinet.domain.dataset.FilterField netField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.FilterField(
                dataField.getValue().toString(), dataField.getAllowedNets(), dataField.getFilterMetadata()));
    }

    protected Optional<DataField> transformStringCollectionField(com.netgrif.application.engine.objects.workflow.domain.DataField dataField,
                                                                 com.netgrif.application.engine.objects.petrinet.domain.dataset.StringCollectionField netField) {
        if (dataField.getValue() != null && dataField.getValue() instanceof Collection<?> dataFieldValue && !dataFieldValue.isEmpty()) {
            List<String> dataFieldValueAsList = dataFieldValue.stream().map(String::valueOf).toList();
            return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.StringCollectionField(dataFieldValueAsList));
        }
        return Optional.empty();
    }

    protected Optional<DataField> transformEnumerationMapField
            (com.netgrif.application.engine.objects.workflow.domain.DataField enumMap, EnumerationMapField netField) {
        Map<String, I18nString> options = this.getFieldOptions(enumMap, netField);
        String selectedKey = (String) enumMap.getValue();
        I18nString selectedValue = options.get(selectedKey);
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.MapField(
                new AbstractMap.SimpleEntry<>(selectedKey, selectedValue != null ? selectedValue : new I18nString(""))));
    }

    private Map<String, I18nString> getFieldOptions
            (com.netgrif.application.engine.objects.workflow.domain.DataField map, MapOptionsField<I18nString, ?> netField) {
        if (map.getOptions() != null) {
            return map.getOptions();
        }
        return netField.getOptions();
    }

    protected Optional<DataField> transformMultichoiceField
            (com.netgrif.application.engine.objects.workflow.domain.DataField multichoiceField, MultichoiceField netField) {
        Optional<Set> optValues = this.getMultichoiceValue(multichoiceField, netField);
        if (optValues.isEmpty()) {
            return Optional.empty();
        }
        Set values = optValues.get();

        List<String> translations = new ArrayList<>();
        values.forEach(value -> {
            if (value instanceof I18nString) {
                translations.addAll(I18nStringUtils.collectTranslations((I18nString) value));
            } else if (value instanceof String) {
                translations.add((String) value);
            } else {
                // TODO vyhodit exception?
                log.error("MultichoiceField has element value of illegal type! Expected: I18nString, Found: " + value.getClass().getCanonicalName());
            }
        });
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TextField(translations));
    }

    private Optional<Set> getMultichoiceValue(com.netgrif.application.engine.objects.workflow.domain.DataField
                                                      multichoice, Field netField) {
        if (multichoice.getValue() instanceof Set) {
            return Optional.of((Set) multichoice.getValue());
        } else if (multichoice.getValue() instanceof Collection) {
//            log.warn(String.format("Multichoice field should have values of type Set! DateField (%s) with %s value found! Value will be converted for indexation.", netField.getImportId(), multichoice.getValue().getClass().getCanonicalName()));
            Set values = new HashSet();
            values.addAll((Collection) multichoice.getValue());
            return Optional.of(values);
        } else {
            // TODO error?
            log.error("Multichoice field has value of illegal type! Expected: Set, Found: " + multichoice.getValue().getClass().getCanonicalName());
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformEnumerationField
            (com.netgrif.application.engine.objects.workflow.domain.DataField enumField) {
        Object value = enumField.getValue();
        if (value instanceof I18nString) {
            return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TextField(I18nStringUtils.collectTranslations((I18nString) value)));
        } else if (value instanceof String) {
            return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TextField((String) value));
        } else {
            // TODO vyhodit exception?
            log.error("Enumeration field has value of illegal type! Expected: I18nString, Found: " + value.getClass().getCanonicalName());
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformNumberField
            (com.netgrif.application.engine.objects.workflow.domain.DataField numberField) {
        if (numberField.getValue() instanceof Integer) { //TODO: Refactor
            return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.NumberField(Double.parseDouble(numberField.getValue().toString())));
        }
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.NumberField((Double) numberField.getValue()));
    }

    protected Optional<DataField> transformButtonField
            (com.netgrif.application.engine.objects.workflow.domain.DataField buttonField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.ButtonField((Integer) buttonField.getValue()));
    }

    protected Optional<DataField> transformActorField
            (com.netgrif.application.engine.objects.workflow.domain.DataField actorField) {
        ActorFieldValue actorFieldValue = (ActorFieldValue) actorField.getValue();
        if (actorFieldValue == null)
            return Optional.empty();
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.ActorField(actorFieldValue.buildMappingData()));
    }

    protected Optional<DataField> transformActorListField
            (com.netgrif.application.engine.objects.workflow.domain.DataField actorListField) {
        ActorListFieldValue actorListFieldValue = (ActorListFieldValue) actorListField.getValue();
        List<ActorMappingData> actorMappingDataList = actorListFieldValue.getActorValues().stream()
                .map(ActorFieldValue::buildMappingData)
                .toList();
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.ActorListField(actorMappingDataList));
    }

    protected Optional<DataField> transformTaskField(com.netgrif.application.engine.objects.workflow.domain.DataField dataField) {
        String[] referencedTasks = ((List<String>) dataField.getValue()).toArray(new String[0]);
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TaskField(referencedTasks));
    }

    protected Optional<DataField> transformDateField
            (com.netgrif.application.engine.objects.workflow.domain.DataField
                     dateField, com.netgrif.application.engine.objects.petrinet.domain.dataset.DateField netField) {
        if (dateField.getValue() instanceof LocalDate date) {
            return formatDateField(LocalDateTime.of(date, LocalTime.MIDNIGHT));
        } else if (dateField.getValue() instanceof Date) {
//            log.warn(String.format("DateFields should have LocalDate values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
            LocalDateTime transformed = this.transformDateValueField(dateField);
            return formatDateField(LocalDateTime.of(transformed.toLocalDate(), LocalTime.MIDNIGHT));
        } else {
            // TODO throw error?
            log.error(String.format("Unsupported DateField value type (%s)! Skipping indexation...", dateField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformDateTimeField
            (com.netgrif.application.engine.objects.workflow.domain.DataField dateTimeField, DateTimeField netField) {
        if (dateTimeField.getValue() instanceof LocalDateTime) {
            return formatDateField((LocalDateTime) dateTimeField.getValue());
        } else if (dateTimeField.getValue() instanceof Date) {
//            log.warn(String.format("DateTimeFields should have LocalDateTime values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
            return formatDateField(this.transformDateValueField(dateTimeField));
        } else {
            // TODO throw error?
            log.error(String.format("Unsupported DateTimeField value type (%s)! Skipping indexation...", dateTimeField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    private LocalDateTime transformDateValueField(com.netgrif.application.engine.objects.workflow.domain.DataField
                                                          dateValueField) {
        return ((Date) dateValueField.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Optional<DataField> formatDateField(LocalDateTime date) {
        if (date == null)
            return Optional.empty();
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.DateField(date.format(DateTimeFormatter.BASIC_ISO_DATE), date));
    }

    protected Optional<DataField> transformBooleanField
            (com.netgrif.application.engine.objects.workflow.domain.DataField booleanField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.BooleanField((Boolean) booleanField.getValue()));
    }

    protected Optional<DataField> transformTextField
            (com.netgrif.application.engine.objects.workflow.domain.DataField textField) {
        if (textField.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TextField((String) textField.getValue()));
    }

    protected Optional<DataField> transformFileField
            (com.netgrif.application.engine.objects.workflow.domain.DataField fileField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.FileField((FileFieldValue) fileField.getValue()));
    }

    protected Optional<DataField> transformFileListField
            (com.netgrif.application.engine.objects.workflow.domain.DataField fileListField) {
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.FileField(((FileListFieldValue) fileListField.getValue()).getNamesPaths()));
    }

    protected Optional<DataField> transformOtherFields
            (com.netgrif.application.engine.objects.workflow.domain.DataField otherField, Field netField) {
        log.warn("Field of type " + netField.getClass().getCanonicalName() + " is not supported for indexation by default. Indexing the toString() representation of its value...");
        return Optional.of(new com.netgrif.application.engine.adapter.spring.elastic.domain.TextField(otherField.getValue().toString()));
    }
}
