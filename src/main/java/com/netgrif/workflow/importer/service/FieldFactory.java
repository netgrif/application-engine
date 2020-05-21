package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.importer.model.*;
import com.netgrif.workflow.petrinet.domain.Format;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.petrinet.domain.views.View;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class FieldFactory {

    @Autowired
    private FormatFactory formatFactory;

    @Autowired
    private ViewFactory viewFactory;

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
                if (data.getOptions() != null) {
                    field = buildEnumerationField(data.getOptions(), data.getInit(), importer);
                } else {
                    field = buildEnumerationField(data.getValues(), data.getInit(), importer);
                }
                break;
            case MULTICHOICE:
                if (data.getOptions() != null) {
                    field = buildMultichoiceField(data.getOptions(), data.getInit(), importer);
                } else {
                    field = buildMultichoiceField(data.getValues(), data.getInit(), importer);
                }
                break;
            case NUMBER:
                field = new NumberField();
                break;
            case USER:
                field = buildUserField(data, importer);
                break;
            case CASE_REF:
                field = buildCaseField(data);
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
        if (data.getInit() != null && field instanceof FieldWithDefault)
            setFieldDefaultValue((FieldWithDefault) field, data.getInit());

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

    private MultichoiceField buildMultichoiceField(Options options, String init, Importer importer) {
        Map<String, I18nString> choices = options.getOption().stream()
                .collect(Collectors.toMap(Option::getKey, value -> new I18nString(value.getValue())));

        MultichoiceField field = new MultichoiceField(choices);
        field.setDefaultValue(init);

        return field;
    }

    private MultichoiceField buildMultichoiceField(List<I18NStringType> values, String init, Importer importer) {
        Map<String, I18nString> choices = values.stream()
                .map(importer::toI18NString)
                .collect(Collectors.toMap(I18nString::toString, Function.identity()));
        MultichoiceField field = new MultichoiceField(choices);
        field.setDefaultValue(init);

        return field;
    }

    private EnumerationField buildEnumerationField(Options options, String init, Importer importer) {
        Map<String, I18nString> choices = options.getOption().stream()
                .collect(Collectors.toMap(Option::getKey, value -> new I18nString(value.getValue())));

        EnumerationField field = new EnumerationField(choices);
        field.setDefaultValue(init);

        return field;
    }

    private EnumerationField buildEnumerationField(List<I18NStringType> values, String init, Importer importer) {
        Map<String, I18nString> choices = values.stream()
                .map(importer::toI18NString)
                .collect(Collectors.toMap(I18nString::toString, Function.identity()));

        EnumerationField field = new EnumerationField(choices);
        field.setDefaultValue(init);

        return field;
    }

    private TextField buildTextField(List<I18NStringType> values) {
        String value = null;
        if (values != null && !values.isEmpty())
            value = values.get(0).getValue();

        return new TextField(value);
    }

    private CaseField buildCaseField(Data data) {
        AllowedNets nets = data.getAllowedNets();
        if (nets == null) {
            return new CaseField();
        } else {
            return new CaseField(new ArrayList<>(nets.getAllowedNet()));
        }
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
        Map<String, I18nString> choices = useCase.getDataField(field.getImportId()).getChoices();
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
            Set<String> roles = userField.getChoices().values().stream().map(I18nString::getDefaultValue).collect(Collectors.toSet());
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
            for (I18nString i18nString : field.getChoices().values()) {
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
            throw new IllegalArgumentException("Object " + value.toString() + " cannot be set as value to the File field [" + fieldId + "] !");
    }
}