package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.importer.model.*;
import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.DocumentRef;
import com.netgrif.workflow.importer.model.I18NStringType;
import com.netgrif.workflow.petrinet.domain.Format;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.petrinet.domain.views.View;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public final class FieldFactory {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private FormatFactory formatFactory;

    @Autowired
    private ViewFactory viewFactory;

    // TODO: refactor this shit
    Field getField(Data data, Importer importer) throws IllegalArgumentException {
        Field field;
        switch (data.getType()) {
            case TEXT:
                field = buildTextField(data.getValues());
                break;
            case BOOLEAN:
                field = new BooleanField();
                break;
            case DATE:
                field = new DateField();
                break;
            case FILE:
                field = buildFileField(data, importer);
                break;
            case ENUMERATION:
                    field = buildEnumerationField(data.getValues(), data.getInit(), importer);
                break;
            case MULTICHOICE:
                    field = buildMultichoiceField(data.getValues(), data.getInit(), importer);
                break;
            case NUMBER:
                field = new NumberField();
                break;
            case USER:
                field = buildUserField(data, importer);
                break;
            case CASEREF:
                field = buildCaseField(data, importer);
                break;
            case DATE_TIME:
                field = new DateTimeField();
                break;
            case BUTTON:
                field = new ButtonField();
                break;
            case TASK_REF:
                field = new TaskField();
                break;
            case ENUMERATION_MAP:
                field = buildEnumerationMapField(data.getOptions(), data.getInit(), importer);
                break;
            case MULTICHOICE_MAP:
                field = buildMultichoiceMapField(data.getOptions(), data.getInit(), importer);
                break;
            default:
                throw new IllegalArgumentException(data.getType() + " is not a valid Field type");
        }

        field.setName(importer.toI18NString(data.getTitle()));
        field.setImportId(data.getId());
        field.setImmediate(data.isImmediate());
        if (data.getLength() != null) {
            field.setLength(data.getLength());
        }
        if (data.getDesc() != null)
            field.setDescription(importer.toI18NString(data.getDesc()));

        if (data.getPlaceholder() != null)
            field.setPlaceholder(importer.toI18NString(data.getPlaceholder()));

        if (data.getValid() != null && field instanceof ValidableField){
            List<String> list = data.getValid();
            for (String item : list) {
                ((ValidableField) field).addValidation(item,null);
            }
        }
        if (data.getValidations() != null && field instanceof ValidableField) {
            List<com.netgrif.workflow.importer.model.Validation> list = data.getValidations().getValidation();
            for (com.netgrif.workflow.importer.model.Validation item : list) {
                ((ValidableField) field).addValidation(item.getExpression(), importer.toI18NString(item.getMessage()));
            }
        }
        if (data.getInit() != null && !data.getInit().isEmpty() && field instanceof FieldWithDefault) {
            setFieldDefaultValue((FieldWithDefault) field, data.getInit().get(0));
        }

        if (data.getFormat() != null) {
            Format format = formatFactory.buildFormat(data.getFormat());
            field.setFormat(format);
        }
        if (data.getView() != null) {
            View view = viewFactory.buildView(data);
            field.setView(view);
        }
        setActions(field, data);
        setEncryption(field, data);

        return field;
    }

    private MultichoiceMapField buildMultichoiceMapField(Options options, List<String> init, Importer importer) {
        Map<String, I18nString> choices = options.getOption().stream()
                .collect(Collectors.toMap(Option::getKey, importer::toI18NString));
        return new MultichoiceMapField(choices, new HashSet<>(init));
    }

    private MultichoiceField buildMultichoiceField(List<I18NStringType> values, List<String> init, Importer importer) {
        List<I18nString> choices = values.stream()
                .map(importer::toI18NString)
                .collect(Collectors.toList());
        MultichoiceField field = new MultichoiceField(choices);
        if (init!= null && !init.isEmpty()) {
            field.setDefaultValue(init.get(0));
        }

        return field;
    }

    private EnumerationField buildEnumerationField(List<I18NStringType> values, List<String> init, Importer importer) {
        List<I18nString> choices = values.stream()
                .map(importer::toI18NString)
                .collect(Collectors.toList());

        EnumerationField field = new EnumerationField(choices);
        if (init != null && !init.isEmpty()) {
            field.setDefaultValue(init.get(0));
        }

        return field;
    }

    private EnumerationMapField buildEnumerationMapField(Options options, List<String> init, Importer importer) {
        Map<String, I18nString> choices = options.getOption().stream()
                .collect(Collectors.toMap(Option::getKey, importer::toI18NString));
        EnumerationMapField field = new EnumerationMapField(choices);
        if (init!= null && !init.isEmpty()) {
            field.setDefaultValue(init.get(0));
        }
        return field;
    }

    private TextField buildTextField(List<I18NStringType> values) {
        String value = null;
        if (values != null && !values.isEmpty())
            value = values.get(0).getValue();

        return new TextField(value);
    }

    private CaseField buildCaseField(Data data, Importer importer) {
        Map<String, LinkedHashSet<String>> netIds = new HashMap<>();
        DocumentRef documentRef = data.getDocumentRef();

        PetriNet net = importer.getNetByImportId(documentRef.getId());
        LinkedHashSet<String> fieldIds = new LinkedHashSet<>();

        net.getDataSet().values().forEach(field -> {
            if (documentRef.getFields().contains(field.getImportId())) {
                fieldIds.add(field.getStringId());
            }
        });

        netIds.put(net.getStringId(), fieldIds);
        return new CaseField(netIds);
    }

    private UserField buildUserField(Data data, Importer importer) {
        String[] roles = data.getValues().stream()
                .map(value -> importer.getRoles().get(value.getValue()).getStringId())
                .toArray(String[]::new);
        return new UserField(roles);
    }

    private FileField buildFileField(Data data, Importer importer) {
        FileField fileField = new FileField();
        fileField.setRemote(data.getRemote() != null);
        return fileField;
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

    private void setFieldDefaultValue(FieldWithDefault field, String defaultValue) {
        switch (field.getType()) {
            case DATETIME:
                field.setDefaultValue(parseDateTime(defaultValue));
                break;
            case DATE:
                field.setDefaultValue(parseDate(defaultValue));
                break;
            case BOOLEAN:
                field.setDefaultValue(Boolean.valueOf(defaultValue));
                break;
            case NUMBER:
                field.setDefaultValue(Double.parseDouble(defaultValue));
                break;
            case MULTICHOICE:
                if (field.getDefaultValue() != null)
                    break;
                ((MultichoiceField) field).setDefaultValue(defaultValue);
                break;
            case FILE:
                ((FileField) field).setDefaultValue(defaultValue);
                break;
            case MULTICHOICE_MAP:
                break;
            default:
                field.setDefaultValue(defaultValue);
        }
    }

    public Field buildFieldWithoutValidation(Case useCase, String fieldId) {
        return buildField(useCase, fieldId, false);
    }

    public Field buildFieldWithValidation(Case useCase, String fieldId) {
        return buildField(useCase, fieldId, true);
    }

    private Field buildField(Case useCase, String fieldId, boolean withValidation) {
        Field field = useCase.getPetriNet().getDataSet().get(fieldId);

        resolveDataValues(field, useCase, fieldId);
        if (field instanceof ChoiceField)
            resolveChoices((ChoiceField) field, useCase);
        return field;
    }

    private void resolveChoices(ChoiceField field, Case useCase) {
        Set<I18nString> choices = useCase.getDataField(field.getImportId()).getChoices();
        if (choices == null)
            return;
        field.setChoices(choices);
    }

    public Field buildImmediateField(Case useCase, String fieldId) {
        Field field = useCase.getPetriNet().getDataSet().get(fieldId);
        resolveDataValues(field, useCase, fieldId);
        return field;
    }

    @SuppressWarnings("RedundantCast")
    private void resolveDataValues(Field field, Case useCase, String fieldId) {
        switch (field.getType()) {
            case DATE:
                parseDateValue((DateField) field, fieldId, useCase);
                parseDateDefaultValue((DateField) field);
                break;
            case NUMBER:
                field.setValue(parseNumberValue(useCase, fieldId));
                break;
            case CASEREF:
                parseCaseRefValue((CaseField) field);
                break;
            case ENUMERATION:
                field.setValue(parseEnumValue(useCase, fieldId, (EnumerationField) field));
                break;
            case MULTICHOICE:
                field.setValue(parseMultichoiceValue(useCase, fieldId));
                break;
            case DATETIME:
                parseDateTimeValue((DateTimeField) field, fieldId, useCase);
                break;
            case FILE:
                parseFileValue((FileField) field, useCase, fieldId);
                break;
            case USER:
                parseUserValues((UserField) field, useCase, fieldId);
                break;
            default:
                field.setValue(useCase.getFieldValue(fieldId));
        }
    }

    private void parseUserValues(UserField field, Case useCase, String fieldId) {
        DataField userField = useCase.getDataField(fieldId);
        if (userField.getChoices() != null) {
            Set<String> roles = userField.getChoices().stream().map(I18nString::getDefaultValue).collect(Collectors.toSet());
            field.setRoles(roles);
        }
        field.setValue((User) useCase.getFieldValue(fieldId));
    }

    public static Set<I18nString> parseMultichoiceValue(Case useCase, String fieldId) {
        Object values = useCase.getFieldValue(fieldId);
        if (values instanceof ArrayList) {
            return (Set<I18nString>) ((ArrayList) values).stream().map(val -> new I18nString(val.toString())).collect(Collectors.toSet());
        } else {
            return (Set<I18nString>) values;
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

        List<String> patterns = Arrays.asList("dd.MM.yyyy");
        try {
            return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ex) {
                for (String pattern : patterns) {
                    try {
                        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
                    } catch (DateTimeParseException | IllegalArgumentException exc) {
                        continue;
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
                        } catch (DateTimeParseException | IllegalArgumentException excp) {
                            continue;
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

    private void parseCaseRefValue(CaseField field) {
        Case refCase = workflowService.findOne(field.getValue());
        PetriNet net = refCase.getPetriNet();

        if (field.getConstraintNetIds() == null || !field.getConstraintNetIds().containsKey(net.getImportId()))
            return;

        Map<String, Object> values = field.getConstraintNetIds().get(net.getImportId()).stream().map(otherFieldId -> {
            Optional<Field> optional = net.getDataSet().values().stream().filter(netField -> Objects.equals(netField.getImportId(), otherFieldId)).findFirst();
            if (!optional.isPresent()) {
                throw new IllegalArgumentException("Field [" + otherFieldId + "] not present in net [" + net.getStringId() + "]");
            }
            String fieldStringId = optional.get().getStringId();
            return Pair.of(fieldStringId, refCase.getDataSet().get(fieldStringId).getValue());
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        field.setImmediateFieldValues(values);
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
            throw new IllegalArgumentException("Object " + value.toString() + " cannot be set as value to the File field [" + fieldId + "] !");
    }
}