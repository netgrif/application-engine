package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.validation.domain.ValidationDataInput

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

class DateTimeFieldValidation extends AbstractFieldValidation {

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
        LocalDateTime updateDate_TODAY = validationData.getData().getLastModified()
        List<String> regex = validationData.getValidationRegex().trim().split(",")
        if (regex.size() == 2) {
            def fromDate = parseStringToLocalDateTime(regex.get(0)) != null ? parseStringToLocalDateTime(regex.get(0)) : regex.get(0)
            def toDate = parseStringToLocalDateTime(regex.get(1)) != null ? parseStringToLocalDateTime(regex.get(1)) : regex.get(1)
            if ((fromDate == TODAY || fromDate == NOW) && toDate == FUTURE) {
                if ((validationData.getData().getValue() as LocalDateTime) < updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == PAST && (toDate == TODAY || toDate == NOW)) {
                if ((validationData.getData().getValue() as LocalDateTime) > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == PAST && (toDate instanceof LocalDateTime)) {
                if ((validationData.getData().getValue() as LocalDateTime) > toDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (fromDate == TODAY && (toDate instanceof LocalDateTime)) {
                if ((validationData.getData().getValue() as LocalDateTime) < toDate || (validationData.getData().getValue() as LocalDateTime) > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if ((fromDate instanceof LocalDateTime) && toDate == TODAY) {
                if ((validationData.getData().getValue() as LocalDateTime) < fromDate || (validationData.getData().getValue() as LocalDateTime) > updateDate_TODAY) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if (toDate == FUTURE && (fromDate instanceof LocalDateTime)) {
                if ((validationData.getData().getValue() as LocalDateTime) < fromDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } else if ((fromDate instanceof LocalDateTime) && (toDate instanceof LocalDateTime)) {
                if ((validationData.getData().getValue() as LocalDateTime) > toDate || (validationData.getData().getValue() as LocalDateTime) < fromDate) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            }
        }
    }

    void workday(ValidationDataInput validationData) {
        if (isWeekend(validationData.getData().getValue() as LocalDateTime)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

    void weekend(ValidationDataInput validationData) {
        if (!isWeekend(validationData.getData().getValue() as LocalDateTime)) {
            throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
        }
    }

    protected static boolean isWeekend(LocalDateTime day) {
        DayOfWeek dayOfWeek = DayOfWeek.of(day.get(ChronoField.DAY_OF_WEEK));
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    protected static LocalDateTime parseStringToLocalDateTime(String stringDate) {
        if (stringDate == null)
            return null

        List<String> patterns = Arrays.asList("dd.MM.yyyy")
        try {
            return LocalDate.parse(stringDate, DateTimeFormatter.BASIC_ISO_DATE)
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(stringDate, DateTimeFormatter.ISO_DATE)
            } catch (DateTimeParseException ex) {
                for (String pattern : patterns) {
                    try {
                        return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(pattern))
                    } catch (DateTimeParseException | IllegalArgumentException exc) {
                        continue
                    }
                }
            }
        }
        return null
    }
}
