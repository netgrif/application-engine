package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Valid;
import com.netgrif.application.engine.importer.service.builder.FieldBuilder;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.validation.IDataValidator;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataValidationExpressionEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Slf4j
public final class FieldFactory {

    private final ComponentFactory componentFactory;

    private final IDataValidator dataValidator;

    private final IDataValidationExpressionEvaluator dataValidationExpressionEvaluator;

    private final Map<DataType, FieldBuilder<?>> builders;

    public FieldFactory(List<FieldBuilder<?>> builders, ComponentFactory componentFactory, IDataValidator dataValidator, IDataValidationExpressionEvaluator dataValidationExpressionEvaluator) {
        this.builders = builders.stream().collect(Collectors.toMap(FieldBuilder::getType, Function.identity()));
        this.componentFactory = componentFactory;
        this.dataValidator = dataValidator;
        this.dataValidationExpressionEvaluator = dataValidationExpressionEvaluator;
    }

    Field getField(Data data, Importer importer) throws IllegalArgumentException, MissingIconKeyException {
        FieldBuilder<?> builder = builders.get(data.getType());
        if (builder == null) {
            throw new IllegalArgumentException("Field " + data.getId() + " has unsupported type " + data.getType());
        }
        Field field = builder.build(data, importer);
        field.setName(importer.toI18NString(data.getTitle()));
        field.setImportId(data.getId());
        if (data.isImmediate() != null) {
            field.setImmediate(data.isImmediate());
        }
        if (data.getLength() != null) {
            field.setLength(data.getLength());
        }
        if (data.getDesc() != null)
            field.setDescription(importer.toI18NString(data.getDesc()));

        if (data.getPlaceholder() != null)
            field.setPlaceholder(importer.toI18NString(data.getPlaceholder()));

        if (data.getValid() != null) {
            List<Valid> list = data.getValid();
            for (Valid item : list) {
                field.addValidation(makeValidation(item.getValue(), null, item.isDynamic()));
            }
        }
        if (data.getValidations() != null) {
            List<com.netgrif.application.engine.importer.model.Validation> list = data.getValidations().getValidation();
            for (com.netgrif.application.engine.importer.model.Validation item : list) {
                field.addValidation(makeValidation(item.getExpression().getValue(), importer.toI18NString(item.getMessage()), item.getExpression().isDynamic()));
            }
        }

        // TODO: NAE-1645 change to component
//        if (data.getFormat() != null) {
//            Format format = formatFactory.buildFormat(data.getFormat());
//            field.setFormat(format);
//        }
//        if (data.getView() != null) {
//            View view = viewFactory.buildView(data);
//            field.setView(view);
//        }

        if (data.getComponent() != null) {
            Component component = componentFactory.buildComponent(data.getComponent(), importer, data);
            field.setComponent(component);
        }

        setActions(field, data);
        setEncryption(field, data);

        dataValidator.checkDeprecatedAttributes(data);
        return field;
    }

    private com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation makeValidation(String rule, I18nString message, boolean dynamic) {
        return dynamic ? new DynamicValidation(rule, message) : new com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation(rule, message);
    }

    private void setActions(Field field, Data data) {
        if (data.getAction() != null && data.getAction().size() != 0) {
//            data.getAction().forEach(action -> field.addAction(action.getValue(), action.getTrigger()));
        }
    }

    private void setEncryption(Field field, Data data) {
        if (data.getEncryption() != null && data.getEncryption().isValue()) {
            String encryption = data.getEncryption().getAlgorithm();
            if (encryption == null)
                encryption = "PBEWITHSHA256AND256BITAES-CBC-BC";
            field.setEncryption(encryption);
        }
    }

    public DataRef buildDataRefWithoutValidation(Case useCase, String fieldId, String transitionId) {
        return buildDataRef(useCase, fieldId, false, transitionId);
    }

    public DataRef buildDataRefWithValidation(Case useCase, String fieldId, String transitionId) {
        return buildDataRef(useCase, fieldId, true, transitionId);
    }

    private DataRef buildDataRef(Case useCase, String fieldId, boolean withValidation, String transitionId) {
        Field field = useCase.getPetriNet().getDataSet().get(fieldId);
        resolveDataValues(field, useCase, fieldId);
        resolveComponent(field, useCase, transitionId);
        if (field instanceof ChoiceField)
            resolveChoices((ChoiceField) field, useCase);
        if (field instanceof MapOptionsField)
            resolveMapOptions((MapOptionsField) field, useCase);
        if (field instanceof FieldWithAllowedNets)
            resolveAllowedNets((FieldWithAllowedNets) field, useCase);
        if (field instanceof FilterField)
            resolveFilterMetadata((FilterField) field, useCase);
        if (withValidation)
            resolveValidations(field, useCase);
        return field;
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    private void resolveValidations(Field field, Case useCase) {
        List<com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation> validations = useCase.getDataField(field.getImportId()).getValidations();
        if (validations != null) {
            field.setValidations(validations.stream().map(it -> it.clone()).collect(Collectors.toList()));
        }
        if (field.getValidations() == null) return;

        ((List<com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation>) field.getValidations()).stream()
                .filter(it -> it instanceof DynamicValidation).map(it -> (DynamicValidation) it).forEach(valid -> {
                    valid.setCompiledRule(dataValidationExpressionEvaluator.compile(useCase, valid.getExpression()));
                });
    }

    private void resolveChoices(ChoiceField field, Case useCase) {
        Set<I18nString> choices = useCase.getDataField(field.getImportId()).getChoices();
        if (choices == null)
            return;
        field.setChoices(choices);
    }

    private void resolveComponent(Field field, Case useCase, String transitionId) {
        if (transitionId == null) {
            return;
        }
        com.netgrif.application.engine.petrinet.domain.Transition transition = useCase.getPetriNet().getTransition(transitionId);
        Component transitionComponent = transition.getDataSet().get(field.getImportId()).getComponent();
        if (transitionComponent != null) {
            field.setComponent(transitionComponent);
        }
    }

    private void resolveMapOptions(MapOptionsField<I18nString, ?> field, Case useCase) {
        Map<String, I18nString> options = useCase.getDataField(field.getImportId()).getOptions();
        if (options == null)
            return;
        field.setOptions(options);
    }

    private void resolveAllowedNets(FieldWithAllowedNets<?> field, Case useCase) {
        List<String> allowedNets = useCase.getDataField(field.getImportId()).getAllowedNets();
        if (allowedNets == null)
            return;
        field.setAllowedNets(allowedNets);
    }

    private void resolveFilterMetadata(FilterField field, Case useCase) {
        Map<String, Object> metadata = useCase.getDataField(field.getImportId()).getFilterMetadata();
        if (metadata == null)
            return;
        field.setFilterMetadata(metadata);
    }

    public Field buildImmediateField(Case useCase, String fieldId) {
        Field field = useCase.getPetriNet().getDataSet().get(fieldId);
        resolveDataValues(field, useCase, fieldId);
        resolveAttributeValues(field, useCase, fieldId);
        return field;
    }

    private void resolveDataValues(Field field, Case useCase, String fieldId) {
        switch (field.getType()) {
            case DATE:
                parseDateValue((DateField) field, fieldId, useCase);
                parseDateDefaultValue((DateField) field);
                break;
            case NUMBER:
                field.setValue(parseNumberValue(useCase, fieldId));
                break;
            case ENUMERATION:
                field.setValue(parseEnumValue(useCase, fieldId, (EnumerationField) field));
                break;
            case MULTICHOICE_MAP:
                field.setValue(parseMultichoiceMapValue(useCase, fieldId));
                break;
            case MULTICHOICE:
                field.setValue(parseMultichoiceValue(useCase, fieldId));
                break;
            case DATE_TIME:
                parseDateTimeValue((DateTimeField) field, fieldId, useCase);
                break;
            case FILE:
                parseFileValue((FileField) field, useCase, fieldId);
                break;
            case FILE_LIST:
                parseFileListValue((FileListField) field, useCase, fieldId);
                break;
            case USER:
                parseUserValues((UserField) field, useCase, fieldId);
                break;
            case TEXT:
                field.setValue(useCase.getFieldValue(fieldId));
            case BUTTON:
                field.setValue(useCase.getFieldValue(fieldId));
            case FILTER:
                field.setValue(useCase.getFieldValue(fieldId));
            case I_18_N:
                field.setValue(useCase.getFieldValue(fieldId));
            case BOOLEAN:
                field.setValue(useCase.getFieldValue(fieldId));
            case CASE_REF:
                field.setValue(useCase.getFieldValue(fieldId));
            case TASK_REF:
                field.setValue(useCase.getFieldValue(fieldId));
            case USER_LIST:
                field.setValue(useCase.getFieldValue(fieldId));
            case ENUMERATION_MAP:
                field.setValue(useCase.getFieldValue(fieldId));
        }
    }

    private void parseUserValues(UserField field, Case useCase, String fieldId) {
        DataField userField = useCase.getDataField(fieldId);
        if (userField.getChoices() != null) {
            Set<String> roles = userField.getChoices().stream().map(I18nString::getDefaultValue).collect(Collectors.toSet());
            field.setRoles(roles);
        }
        field.setValue((UserFieldValue) useCase.getFieldValue(fieldId));
    }

    public static Set<I18nString> parseMultichoiceValue(Case useCase, String fieldId) {
        Object values = useCase.getFieldValue(fieldId);
        if (values instanceof ArrayList) {
            return (Set<I18nString>) ((ArrayList) values).stream().map(val -> new I18nString(val.toString())).collect(Collectors.toSet());
        } else {
            return (Set<I18nString>) values;
        }
    }

    public static Set<String> parseMultichoiceMapValue(Case useCase, String fieldId) {
        Object values = useCase.getFieldValue(fieldId);
        if (values instanceof ArrayList) {
            return ((ArrayList<?>) values).stream().map(val -> val.toString()).collect(Collectors.toSet());
        } else {
            return (Set<String>) values;
        }
    }

    private Double parseNumberValue(Case useCase, String fieldId) {
        Object value = useCase.getFieldValue(fieldId);
        return parseDouble(value);
    }

    public static Double parseDouble(Object value) {
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (value instanceof Integer) {
            return ((Integer) value) * 1D;
        } else if (value instanceof Double) {
            return (Double) value;
        }
        return null;
    }

    private void parseDateValue(DateField field, String fieldId, Case useCase) {
        Object value = useCase.getFieldValue(fieldId);
        field.setValue(parseDate(value));
    }

    private void parseDateDefaultValue(DateField field) {
        Object value = field.getDefaultValue();
        field.setDefaultValue(parseDate(value));
    }

    public static LocalDate parseDate(Object value) {
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (value instanceof String) {
            return parseDateFromString((String) value);
        } else if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }

    /**
     * Available formats - YYYYMMDD; YYYY-MM-DD; DD.MM.YYYY
     *
     * @param value - Date as string
     * @return Parsed date as LocalDate object or null if date cannot be parsed
     */
    public static LocalDate parseDateFromString(String value) {
        if (value == null)
            return null;

        List<String> patterns = List.of("dd.MM.yyyy");
        try {
            return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                for (String pattern : patterns) {
                    try {
                        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
                    } catch (DateTimeParseException | IllegalArgumentException ignored) {
                    }
                }
            }
        }
        return null;
    }

    private void parseDateTimeValue(DateTimeField field, String fieldId, Case useCase) {
        Object value = useCase.getFieldValue(fieldId);
        field.setValue(parseDateTime(value));
    }

    public static LocalDateTime parseDateTime(Object value) {
        if (value == null)
            return null;

        if (value instanceof LocalDate)
            return LocalDateTime.of((LocalDate) value, LocalTime.NOON);
        else if (value instanceof String)
            return parseDateTimeFromString((String) value);
        else if (value instanceof Date)
            return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        return (LocalDateTime) value;
    }

    public static LocalDateTime parseDateTimeFromString(String value) {
        if (value == null)
            return null;

        List<String> patterns = Arrays.asList("dd.MM.yyyy HH:mm", "dd.MM.yyyy HH:mm:ss");
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ex) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ISO_INSTANT);
                } catch (DateTimeParseException exc) {
                    for (String pattern : patterns) {
                        try {
                            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
                        } catch (DateTimeParseException | IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        }
        return null;
    }

    public static I18nString parseEnumValue(Case useCase, String fieldId, EnumerationField field) {
        Object value = useCase.getFieldValue(fieldId);
        if (value instanceof String) {
            for (I18nString i18nString : field.getChoices()) {
                if (i18nString.contains((String) value)) {
                    return i18nString;
                }
            }
            return new I18nString((String) value);
//            throw new IllegalArgumentException("Value " + value + " is not a valid value.");
        } else {
            return (I18nString) value;
        }
    }

    private void parseFileValue(FileField field, Case useCase, String fieldId) {
        Object value = useCase.getFieldValue(fieldId);
        if (value == null)
            return;

        if (value instanceof String) {
            field.setValue((String) value);
        } else if (value instanceof FileFieldValue) {
            field.setValue((FileFieldValue) value);
        } else
            throw new IllegalArgumentException("Object " + value + " cannot be set as value to the File field [" + fieldId + "] !");
    }

    private void parseFileListValue(FileListField field, Case useCase, String fieldId) {
        Object value = useCase.getFieldValue(fieldId);
        if (value == null)
            return;

        if (value instanceof String) {
            field.setValue((String) value);
        } else if (value instanceof FileListFieldValue) {
            field.setValue((FileListFieldValue) value);
        } else {
            throw new IllegalArgumentException("Object " + value + " cannot be set as value to the File list field [" + fieldId + "] !");
        }
    }

    private void resolveAttributeValues(Field field, Case useCase, String fieldId) {
        DataField dataField = useCase.getDataSet().get(fieldId);
        if (field.getType().equals(DataType.CASE_REF) || field.getType().equals(DataType.FILTER)) {
            List<String> allowedNets = new ArrayList<>(dataField.getAllowedNets());
            ((FieldWithAllowedNets<?>) field).setAllowedNets(allowedNets);
        }
        if (field.getType().equals(DataType.FILTER)) {
            Map<String, Object> filterMetadata = new HashMap<>(dataField.getFilterMetadata());
            ((FilterField) field).setFilterMetadata(filterMetadata);
        }
    }
}