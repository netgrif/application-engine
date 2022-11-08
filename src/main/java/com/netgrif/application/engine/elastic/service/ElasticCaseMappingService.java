package com.netgrif.application.engine.elastic.service;


import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.domain.BooleanField;
import com.netgrif.application.engine.elastic.domain.ButtonField;
import com.netgrif.application.engine.elastic.domain.DateField;
import com.netgrif.application.engine.elastic.domain.FileField;
import com.netgrif.application.engine.elastic.domain.NumberField;
import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.elastic.domain.UserField;
import com.netgrif.application.engine.elastic.domain.*;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.ml.inference.preprocessing.Multi;
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
        Field caseField = useCase.getDataField(fieldId);

//        TODO: NAE-1645 refactor using registry
        if (caseField.getValue() == null) {
            return Optional.empty();
        } else if (netField instanceof EnumerationMapField) {
            return this.transformEnumerationMapField((EnumerationMapField) caseField, (EnumerationMapField) netField);
        } else if (netField instanceof MultichoiceMapField) {
            return this.transformMultichoiceMapField((MultichoiceMapField) caseField, (MultichoiceMapField) netField);
        } else if (netField instanceof MultichoiceField) {
            return this.transformMultichoiceField(caseField, (MultichoiceField) netField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.NumberField) {
            return this.transformNumberField((com.netgrif.application.engine.petrinet.domain.dataset.NumberField) caseField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.ButtonField) {
            return this.transformButtonField((com.netgrif.application.engine.petrinet.domain.dataset.ButtonField) caseField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.UserField) {
            return this.transformUserField((com.netgrif.application.engine.petrinet.domain.dataset.UserField) caseField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.DateField) {
            return this.transformDateField((com.netgrif.application.engine.petrinet.domain.dataset.DateField) caseField, (com.netgrif.application.engine.petrinet.domain.dataset.DateField) netField);
        } else if (netField instanceof DateTimeField) {
            return this.transformDateTimeField((DateTimeField) caseField, (DateTimeField) netField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.BooleanField) {
            return this.transformBooleanField((com.netgrif.application.engine.petrinet.domain.dataset.BooleanField) caseField);
        } else if (netField instanceof EnumerationField) {
            return this.transformEnumerationField(caseField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.TextField) {
            return this.transformTextField((com.netgrif.application.engine.petrinet.domain.dataset.TextField) caseField);
        } else if (netField instanceof com.netgrif.application.engine.petrinet.domain.dataset.FileField) {
            return this.transformFileField((com.netgrif.application.engine.petrinet.domain.dataset.FileField) caseField);
        } else if (netField instanceof FileListField) {
            return this.transformFileListField((FileListField) caseField);
        } else if (netField instanceof UserListField) {
            return this.transformUserListField((UserListField) caseField);
        } else {
            String string = caseField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return this.transformOtherFields(caseField, netField);
        }
    }

    protected Optional<DataField> transformMultichoiceMapField(MapOptionsField<I18nString, ?> multichoiceMap, MultichoiceMapField netField) {
        Optional<Set> optValues = this.getMultichoiceValue(multichoiceMap, netField);
        if (!optValues.isPresent()) {
            return Optional.empty();
        }
        Set mapValues = optValues.get();
        Map<String, I18nString> options = this.getFieldOptions(multichoiceMap, netField);
        List<Map.Entry<String, Collection<String>>> values = new ArrayList<>();
        for (String key : (Set<String>) mapValues) {
            values.add(new AbstractMap.SimpleEntry<>(key, collectTranslations(options.get(key))));
        }
        return Optional.of(new MapField(values));
    }

    protected Optional<DataField> transformEnumerationMapField(MapOptionsField<I18nString, String> enumMap, EnumerationMapField netField) {
        Map<String, I18nString> options = this.getFieldOptions(enumMap, netField);
        DataFieldValue<String> selectedKey = enumMap.getValue();
        String value = selectedKey != null ? selectedKey.getValue() : null;
        return Optional.of(new MapField(new AbstractMap.SimpleEntry<>(value, collectTranslations(options.get(value)))));
    }

    private Map<String, I18nString> getFieldOptions(MapOptionsField<I18nString, ?> map, MapOptionsField<I18nString, ?> netField) {
        if (map.getOptions() != null) {
            return map.getOptions();
        }
        return netField.getOptions();
    }

    protected Optional<DataField> transformMultichoiceField(Field multichoiceField, MultichoiceField netField) {
        Optional<Set> optValues = this.getMultichoiceValue(multichoiceField, netField);
        if (!optValues.isPresent()) {
            return Optional.empty();
        }
        Set values = optValues.get();

        List<String> translations = new ArrayList<>();
        values.forEach(value -> {
            if (value instanceof I18nString) {
                translations.addAll(this.collectTranslations((I18nString) value));
            } else if (value instanceof String) {
                translations.add((String) value);
            } else {
                // TODO vyhodit exception?
                log.error("MultichoiceField has element value of illegal type! Expected: I18nString, Found: " + value.getClass().getCanonicalName());
            }
        });
        return Optional.of(new TextField(translations.toArray(new String[0])));
    }

    private Optional<Set> getMultichoiceValue(Field multichoice, Field netField) {
        if (multichoice.getValue() instanceof Set) {
            return Optional.of((Set) multichoice.getValue());
        } else if (multichoice.getValue() instanceof Collection) {
            log.warn(String.format("Multichoice field should have values of type Set! DateField (%s) with %s value found! Value will be converted for indexation.", netField.getImportId(), multichoice.getValue().getClass().getCanonicalName()));
            Set values = new HashSet();
            values.addAll((Collection) multichoice.getValue());
            return Optional.of(values);
        } else {
            // TODO error?
            log.error("Multichoice field has value of illegal type! Expected: Set, Found: " + multichoice.getValue().getClass().getCanonicalName());
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformEnumerationField(Field enumField) {
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

    protected Optional<DataField> transformNumberField(com.netgrif.application.engine.petrinet.domain.dataset.NumberField numberField) {
//        TODO: NAE-1645 fix
        if (numberField.getValue().getValue() instanceof Double) { //TODO: Refactor
            return Optional.of(new NumberField(Double.parseDouble(numberField.getValue().toString())));
        }
        return Optional.of(new NumberField(numberField.getValue().getValue()));
    }

    protected Optional<DataField> transformButtonField(com.netgrif.application.engine.petrinet.domain.dataset.ButtonField buttonField) {
        return Optional.of(new ButtonField(buttonField.getValue().getValue()));
    }

    protected Optional<DataField> transformUserField(com.netgrif.application.engine.petrinet.domain.dataset.UserField userField) {
        UserFieldValue user = userField.getValue().getValue();
        if (user == null)
            return Optional.empty();
        return Optional.of(new UserField(this.transformUserValue(user)));
    }

    protected Optional<DataField> transformUserListField(UserListField userListField) {
        List<String> userIds = (List<String>) userListField.getValue();
        List<IUser> users = this.userService.findAllByIds(new HashSet<>(userIds), true);
        return Optional.of(new UserField(users.stream().map(this::transformUserValue).toArray(UserField.UserMappingData[]::new)));
    }

    private UserField.UserMappingData transformUserValue(UserFieldValue user) {
        return new UserField.UserMappingData(user.getId(), user.getEmail(), buildFullName(user.getName(), user.getSurname()).toString());
    }

    private UserField.UserMappingData transformUserValue(IUser user) {
        return new UserField.UserMappingData(user.getStringId(), user.getEmail(), buildFullName(user.getName(), user.getSurname()).toString());
    }

    private StringBuilder buildFullName(String name, String surname) {
        StringBuilder fullName = new StringBuilder();
        if (name != null) {
            fullName.append(name);
            fullName.append(" ");
        }
        if (surname != null) {
            fullName.append(surname);
        }
        return fullName;
    }

    protected Optional<DataField> transformDateField(com.netgrif.application.engine.petrinet.domain.dataset.DateField dateField, com.netgrif.application.engine.petrinet.domain.dataset.DateField netField) {
        if (dateField.getValue().getValue() instanceof LocalDate) {
            LocalDate date = dateField.getValue().getValue();
            return formatDateField(LocalDateTime.of(date, LocalTime.NOON));
//        } else if (dateField.getValue().getValue() instanceof Date) {
//            log.warn(String.format("DateFields should have LocalDate values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
//            LocalDateTime transformed = this.transformDateValueField(dateField);
//            return formatDateField(LocalDateTime.of(transformed.toLocalDate(), LocalTime.NOON));
        } else {
            // TODO throw error?
            log.error(String.format("Unsupported DateField value type (%s)! Skipping indexation...", dateField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    protected Optional<DataField> transformDateTimeField(DateTimeField dateTimeField, DateTimeField netField) {
        if (dateTimeField.getValue().getValue() instanceof LocalDateTime) {
            return formatDateField(dateTimeField.getValue().getValue());
//        } else if (dateTimeField.getValue() instanceof Date) {
//            log.warn(String.format("DateTimeFields should have LocalDateTime values! DateField (%s) with Date value found! Value will be converted for indexation.", netField.getImportId()));
//            return formatDateField(this.transformDateValueField(dateTimeField));
        } else {
//             TODO throw error?
            log.error(String.format("Unsupported DateTimeField value type (%s)! Skipping indexation...", dateTimeField.getValue().getClass().getCanonicalName()));
            return Optional.empty();
        }
    }

    private LocalDateTime transformDateValueField(Field dateValueField) {
        return ((Date) dateValueField.getValue().getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Optional<DataField> formatDateField(LocalDateTime date) {
        if (date == null)
            return Optional.empty();
        return Optional.of(new DateField(date.format(DateTimeFormatter.BASIC_ISO_DATE), date));
    }

    protected Optional<DataField> transformBooleanField(com.netgrif.application.engine.petrinet.domain.dataset.BooleanField booleanField) {
        return Optional.of(new BooleanField(booleanField.getValue().getValue()));
    }

    protected Optional<DataField> transformTextField(com.netgrif.application.engine.petrinet.domain.dataset.TextField textField) {
        if (textField.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(new TextField(textField.getValue().getValue()));
    }

    protected Optional<DataField> transformFileField(com.netgrif.application.engine.petrinet.domain.dataset.FileField fileField) {
        return Optional.of(new FileField(fileField.getValue().getValue()));
    }

    protected Optional<DataField> transformFileListField(FileListField fileListField) {
        return Optional.of(new FileField(fileListField.getValue().getValue().getNamesPaths().toArray(new FileFieldValue[0])));
    }

    protected Optional<DataField> transformOtherFields(Field otherField, Field netField) {
        log.warn("Field of type " + netField.getClass().getCanonicalName() + " is not supported for indexation by default. Indexing the toString() representation of its value...");
        return Optional.of(new TextField(otherField.getValue().toString()));
    }
}
