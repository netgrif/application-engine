package com.netgrif.workflow.elastic.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.elastic.domain.*;
import com.netgrif.workflow.elastic.domain.BooleanField;
import com.netgrif.workflow.elastic.domain.DateField;
import com.netgrif.workflow.elastic.domain.FileField;
import com.netgrif.workflow.elastic.domain.NumberField;
import com.netgrif.workflow.elastic.domain.TextField;
import com.netgrif.workflow.elastic.domain.UserField;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IUserService userService;

    @Override
    public ElasticCase transform(Case useCase) {
        ElasticCase transformedCase = new ElasticCase(useCase);
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
        Field netField = useCase.getField(fieldId);
        com.netgrif.workflow.workflow.domain.DataField caseField = useCase.getDataField(fieldId);

        if (caseField.getValue() == null) {
            return Optional.empty();
        } else if (netField instanceof EnumerationMapField) {
            return this.transformEnumerationMapField(caseField, (EnumerationMapField) netField);
        } else if (netField instanceof MultichoiceMapField) {
            return this.transformMultichoiceMapField(caseField, (MultichoiceMapField) netField);
        } else if (netField instanceof MultichoiceField) {
            return this.transformMultichoiceField(caseField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.NumberField) {
            return this.transformNumberField(caseField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.UserField) {
            return this.transformUserField(caseField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.DateField) {
            return this.transformDateField(caseField, (com.netgrif.workflow.petrinet.domain.dataset.DateField) netField);
        } else if (netField instanceof DateTimeField) {
            return this.transformDateTimeField(caseField, (DateTimeField) netField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.BooleanField) {
            return this.transformBooleanField(caseField);
        } else if (netField instanceof EnumerationField) {
            return this.transformEnumerationField(caseField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.TextField) {
            return this.transformTextField(caseField);
        } else if (netField instanceof com.netgrif.workflow.petrinet.domain.dataset.FileField) {
            return this.transformFileField(caseField);
        } else if (netField instanceof FileListField) {
            return this.transformFileListField(caseField);
        } else if (netField instanceof UserListField) {
            return this.transformUserListField(caseField);
        } else {
            String string = caseField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return this.transformOtherFields(caseField, netField);
        }
    }

    protected Optional<DataField> transformMultichoiceMapField(com.netgrif.workflow.workflow.domain.DataField multichoiceMap, MultichoiceMapField netField) {
        Map<String, I18nString> options = this.getFieldOptions(multichoiceMap, netField);
        List<Map.Entry<String, Collection<String>>> values = new ArrayList<>();
        for (String key : (Set<String>) multichoiceMap.getValue()) {
            values.add(new AbstractMap.SimpleEntry<>(key, collectTranslations(options.get(key))));
        }
        return Optional.of(new MapField(values));
    }

    protected Optional<DataField> transformEnumerationMapField(com.netgrif.workflow.workflow.domain.DataField enumMap, EnumerationMapField netField) {
        Map<String, I18nString> options = this.getFieldOptions(enumMap, netField);
        String selectedKey = (String) enumMap.getValue();
        return Optional.of(new MapField(new AbstractMap.SimpleEntry<>(selectedKey, collectTranslations(options.get(selectedKey)))));
    }

    private Map<String, I18nString> getFieldOptions(com.netgrif.workflow.workflow.domain.DataField map, MapOptionsField<I18nString, ?> netField) {
        if (map.getOptions() != null) {
            return map.getOptions();
        }
        return netField.getOptions();
    }

    protected Optional<DataField> transformMultichoiceField(com.netgrif.workflow.workflow.domain.DataField multichoiceField) {
        if (multichoiceField.getValue() == null) {
            return Optional.empty();
        }
        Set values = (Set) multichoiceField.getValue();
        List<String> translations = new ArrayList<>();
        values.forEach(value -> {
            if (value instanceof I18nString) {
                translations.addAll(this.collectTranslations((I18nString) value));
            } else if (value instanceof String) {
                translations.add((String) value);
            } else {
                // TODO vyhodit exception?
                log.error("Multichoice field has value of illegal type! Expected: I18nString, Found: " + value.getClass().getCanonicalName());
            }
        });
        return Optional.of(new TextField(translations.toArray(new String[0])));
    }

    protected Optional<DataField> transformEnumerationField(com.netgrif.workflow.workflow.domain.DataField enumField) {
        Object value = enumField.getValue();
        if (value instanceof I18nString) {
            return Optional.of(new TextField(this.collectTranslations((I18nString) value).toArray(new String[0])));
        } else if (value instanceof String) {
            return Optional.of(new TextField((String) value));
        } else {
            // TODO vyhodit exception?
            log.error("Enumeration field has value of illegal type! Expected: I18nString, Found: " + value.getClass().getCanonicalName());
            return Optional.empty();
        }
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
        return Optional.of(new UserField(this.transformUserValue(user)));
    }

    protected Optional<DataField> transformUserListField(com.netgrif.workflow.workflow.domain.DataField userListField) {
        List<Long> userIds = (List<Long>) userListField.getValue();
        List<User> users = this.userService.findAllByIds(new HashSet<>(userIds), true);
        return Optional.of(new UserField(users.stream().map(this::transformUserValue).toArray(UserField.UserMappingData[]::new)));
    }

    private UserField.UserMappingData transformUserValue(User user) {
        StringBuilder fullName = new StringBuilder();
        if (user.getName() != null) {
            fullName.append(user.getName());
            fullName.append(" ");
        }
        if (user.getSurname() != null) {
            fullName.append(user.getSurname());
        }
        return new UserField.UserMappingData(user.getId(), user.getEmail(), fullName.toString());
    }

    protected Optional<DataField> transformDateField(com.netgrif.workflow.workflow.domain.DataField dateField, com.netgrif.workflow.petrinet.domain.dataset.DateField netField) {
        if (dateField.getValue() instanceof LocalDate) {
            LocalDate date = (LocalDate) dateField.getValue();
            return formatDateField(LocalDateTime.of(date, LocalTime.NOON));
        } else if (dateField.getValue() instanceof Date) {
            log.warn(String.format("DateFields should have LocalDate values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
            return this.transformDateValueField(dateField);
        } else {
            // TODO throw error?
            log.error(String.format("Unsupported DateField value type (%s)! Skipping indexation...", dateField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformDateTimeField(com.netgrif.workflow.workflow.domain.DataField dateTimeField, DateTimeField netField) {
        if (dateTimeField.getValue() instanceof LocalDateTime) {
            return formatDateField((LocalDateTime) dateTimeField.getValue());
        } else if (dateTimeField.getValue() instanceof Date) {
            log.warn(String.format("DateTimeFields should have LocalDateTime values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
            return this.transformDateValueField(dateTimeField);
        } else {
            // TODO throw error?
            log.error(String.format("Unsupported DateTimeField value type (%s)! Skipping indexation...", dateTimeField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    private Optional<DataField> transformDateValueField(com.netgrif.workflow.workflow.domain.DataField dateValueField) {
        LocalDateTime date = ((Date) dateValueField.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return this.formatDateField(date);
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

    protected Optional<DataField> transformOtherFields(com.netgrif.workflow.workflow.domain.DataField otherField, Field netField) {
        log.warn("Field of type " + netField.getClass().getCanonicalName() + " is not supported for indexation by default. Indexing the toString() representation of its value...");
        return Optional.of(new TextField(otherField.getValue().toString()));
    }
}
