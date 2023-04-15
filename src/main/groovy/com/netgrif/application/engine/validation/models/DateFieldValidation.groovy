package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.petrinet.domain.dataset.DateField
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import groovy.util.logging.Slf4j

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

@Slf4j
class DateFieldValidation extends AbstractFieldValidation {

//    BETWEEN = 'between'
//    WORKDAY = 'workday'
//    WEEKEND = 'weekend'
//    REQUIRED = 'required',
//    VALID_BETWEEN = 'validBetween',
//    VALID_WORKDAY = 'validWorkday',
//    VALID_WEEKEND = 'validWeekend'

//    between today,future
//    between past,today
//    between 2020-03-03,today

    public static final String FUTURE = "future"
    public static final String TODAY = "today"
    public static final String PAST = "past"
    public static final String NOW = "now"

    void between(ValidationDataInput validationData) {
        LocalDate updateDate_TODAY = LocalDate.now()
        List<String> regex = validationData.getValidationRegex().trim().split(",")
        LocalDate setDate = getDateValue(validationData.getData())

        if (regex.size() == 2) {
            def fromDate = parseStringToLocalDate(regex.get(0)) != null ? parseStringToLocalDate(regex.get(0)) : regex.get(0)
            def toDate = parseStringToLocalDate(regex.get(1)) != null ? parseStringToLocalDate(regex.get(1)) : regex.get(1)
            if ((fromDate == TODAY || fromDate == NOW) && toDate == FUTURE) {
                if (setDate < updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == PAST && (toDate == TODAY || toDate == NOW)) {
                if (setDate > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == PAST && (toDate instanceof LocalDate)) {
                if (setDate > toDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == TODAY && (toDate instanceof LocalDate)) {
                if (setDate < toDate || setDate > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if ((fromDate instanceof LocalDate) && toDate == TODAY) {
                if (setDate < fromDate || setDate > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (toDate == FUTURE && (fromDate instanceof LocalDate)) {
                if (setDate < fromDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if ((fromDate instanceof LocalDate) && (toDate instanceof LocalDate)) {
                if (setDate > toDate || setDate < fromDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            }
        }
    }

    void workday(ValidationDataInput validationData) {
        LocalDate setDate = getDateValue(validationData.getData())
        if (isWeekend(setDate)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

    void weekend(ValidationDataInput validationData) {
        LocalDate setDate = getDateValue(validationData.getData())
        if (!isWeekend(setDate)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

    // TODO: release/7.0.0 Refactor, each type own validator with common functions
    LocalDate getDateValue(Field<?> field) {
        if (field instanceof DateField) {
            return ((DateField) field).getRawValue()
        }
        throw new IllegalArgumentException("Cannot validate field " + field.stringId + " of type " + field.type + " with date validation")
    }

    protected static boolean isWeekend(LocalDate day) {
        DayOfWeek dayOfWeek = DayOfWeek.of(day.get(ChronoField.DAY_OF_WEEK));
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    protected LocalDate parseStringToLocalDate(String stringDate) {
        if (stringDate == null) {
            return null
        }
        List<String> patterns = Arrays.asList("dd.MM.yyyy")
        try {
            return LocalDate.parse(stringDate, DateTimeFormatter.BASIC_ISO_DATE)
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(stringDate, DateTimeFormatter.ISO_DATE)
            } catch (DateTimeParseException ignored2) {
                for (String pattern : patterns) {
                    try {
                        return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(pattern))
                    } catch (DateTimeParseException | IllegalArgumentException ignored3) {}
                }
            }
        }
        return null
    }
}
