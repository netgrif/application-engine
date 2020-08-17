package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.*;

import java.util.Locale;

public class LocalisedFieldFactory {

    // todo: remove this monstrosity
    public static LocalisedField from(Field field, Locale locale) {
        if (field instanceof EnumerationField) {
            return fromEnumeration((EnumerationField) field, locale);
        } else if (field instanceof MultichoiceField) {
            return fromMultichoice((MultichoiceField)   field, locale);
            // case, file,
        } else if (field instanceof NumberField) {
            return fromNumber((NumberField) field, locale);
        } else if (field instanceof TextField) {
            return fromText((TextField) field, locale);
        } else if (field instanceof DateField) {
            return fromDate((DateField) field, locale);
        } else if (field instanceof DateTimeField) {
            return fromDateTime((DateTimeField) field, locale);
        } else if (field instanceof BooleanField) {
            return fromBoolean((BooleanField) field, locale);
        } else if (field instanceof UserField) {
            return fromUser((UserField) field, locale);
        } else if (field instanceof CaseField) {
            return fromCase((CaseField) field, locale);
        } else if (field instanceof FileListField) {
            return fromFileList((FileListField) field, locale);
        } else {
            return fromGeneral(field, locale);
        }
    }

    private static LocalisedField fromGeneral(Field field, Locale locale) {
        return new LocalisedField(field, locale);
    }

    private static LocalisedField fromNumber(NumberField field, Locale locale) {
        return new LocalisedNumberField(field, locale);
    }

    private static LocalisedField fromText(TextField field, Locale locale) {
        return new LocalisedTextField(field, locale);
    }

    private static LocalisedField fromDate(DateField field, Locale locale) {
        return new LocalisedDateField(field, locale);
    }

    private static LocalisedField fromDateTime(DateTimeField field, Locale locale) {
        return new LocalisedDateTimeField(field, locale);
    }

    private static LocalisedField fromBoolean(BooleanField field, Locale locale) {
        return new LocalisedBooleanField(field, locale);
    }

    private static LocalisedField fromUser(UserField field, Locale locale) {
        return new LocalisedUserField(field, locale);
    }

    private static LocalisedField fromMultichoice(MultichoiceField field, Locale locale) {
        return new LocalisedMultichoiceField(field, locale);
    }

    private static LocalisedField fromEnumeration(EnumerationField field, Locale locale) {
        return new LocalisedEnumerationField(field, locale);
    }

    private static LocalisedField fromCase(CaseField field, Locale locale) {
        return new LocalisedCaseField(field, locale);
    }

    public static LocalisedField fromFileList(FileListField field, Locale locale) {
        return new LocalisedFileListField(field, locale);
    }
}