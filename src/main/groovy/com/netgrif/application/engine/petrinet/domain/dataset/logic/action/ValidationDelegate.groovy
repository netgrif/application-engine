package com.netgrif.application.engine.petrinet.domain.dataset.logic.action


import com.netgrif.application.engine.petrinet.domain.dataset.*
import groovy.util.logging.Slf4j

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Slf4j
class ValidationDelegate {

    public static final String FUTURE = 'future'
    public static final String TODAY = 'today'
    public static final String PAST = 'past'
    public static final String NOW = 'now'
    public static final String INF = 'inf'
    public static final String TEL_NUMBER_REGEX = '^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$'
    public static final String EMAIL_REGEX = '^[a-zA-Z0-9\\._\\%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,}$'

    // todo NAE-1788: thisField keyword
    Field<?> thisField

    Boolean notempty() { return thisField.rawValue != null }

    // boolean field validations
    Boolean requiredtrue() { return thisField instanceof BooleanField && notempty() && thisField.rawValue == true }

    // date field validations
    Boolean between(def from, def to) {
        if (!(thisField instanceof DateField || thisField instanceof DateTimeField)) {
            return false
        }

        LocalDateTime updateDate_TODAY = thisField instanceof DateField ? LocalDate.now().atStartOfDay() : LocalDateTime.now()
        LocalDateTime thisFieldValue = thisField.rawValue instanceof LocalDateTime ? thisField.rawValue : thisField.rawValue.atStartOfDay()

        def fromDate = from
        if (from instanceof String) {
            LocalDate parsedDate = parseStringToLocalDate(from)
            fromDate = parsedDate ? parsedDate.atStartOfDay() : from
        }

        def toDate = to
        if (to instanceof String) {
            LocalDate parsedDate = parseStringToLocalDate(to)
            toDate = parsedDate ? parsedDate.atStartOfDay() : to
        }

        if ((fromDate == TODAY || fromDate == NOW) && toDate == FUTURE) {
            if (thisFieldValue < updateDate_TODAY) {
                return false
            }
        } else if (fromDate == PAST && (toDate == TODAY || toDate == NOW)) {
            if (thisFieldValue > updateDate_TODAY) {
                return false
            }
        } else if (fromDate == PAST && (toDate instanceof LocalDateTime)) {
            if (thisFieldValue > toDate) {
                return false
            }
        } else if (fromDate == TODAY && (toDate instanceof LocalDateTime)) {
            if (thisFieldValue > toDate || thisFieldValue < updateDate_TODAY) {
                return false
            }
        } else if ((fromDate instanceof LocalDateTime) && toDate == TODAY) {
            if (thisFieldValue < fromDate || thisFieldValue > updateDate_TODAY) {
                return false
            }
        } else if (toDate == FUTURE && (fromDate instanceof LocalDateTime)) {
            if (thisFieldValue < fromDate) {
                return false
            }
        } else if ((fromDate instanceof LocalDateTime) && (toDate instanceof LocalDateTime)) {
            if (thisFieldValue > toDate || thisFieldValue < fromDate) {
                return false
            }
        }
        return true
    }

    Boolean workday() { return (thisField instanceof DateField || thisField instanceof DateTimeField) && notempty() && !thisField.rawValue.dayOfWeek.isWeekend() }


    Boolean weekend() { return (thisField instanceof DateField || thisField instanceof DateTimeField) && notempty() && thisField.rawValue.dayOfWeek.isWeekend() }

    protected static LocalDate parseStringToLocalDate(String stringDate) {
        if (stringDate == null) {
            return null
        }
        List<String> patterns = Arrays.asList("dd.MM.yyyy", "")
        try {
            return LocalDate.parse(stringDate, DateTimeFormatter.BASIC_ISO_DATE)
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(stringDate, DateTimeFormatter.ISO_DATE)
            } catch (DateTimeParseException ignored2) {
                for (String pattern : patterns) {
                    try {
                        return LocalDateTime.parse(stringDate, DateTimeFormatter.ofPattern(pattern))
                    } catch (DateTimeParseException | IllegalArgumentException ignored3) {
                    }
                }
            }
        }
        return null
    }

    // number field validations
    Boolean odd() { return thisField instanceof NumberField && notempty() && thisField.rawValue as Double % 2 != 0 }

    Boolean even() { return thisField instanceof NumberField && notempty() && thisField.rawValue as Double % 2 == 0 }

    Boolean positive() { return thisField instanceof NumberField && notempty() && thisField.rawValue >= 0 }

    Boolean negative() { return thisField instanceof NumberField && notempty() && thisField.rawValue <= 0 }

    Boolean decimal() { return thisField instanceof NumberField && notempty() && thisField.rawValue as Double % 1 == 0 }

    Boolean inrange(def from, def to) {

        if (from instanceof String && from.toLowerCase() == INF) {
            from = Double.MIN_VALUE
        }

        if (to instanceof String && to.toLowerCase() == INF) {
            to = Double.MAX_VALUE
        }
        return thisField instanceof NumberField && notempty() && thisField.rawValue >= from as Double && thisField.rawValue <= to as Double
    }

    // text field validations
    Boolean regex(String pattern) { return thisField instanceof TextField && notempty() && thisField.rawValue ==~ pattern }

    Boolean minlength(Integer minLength) { return thisField instanceof TextField && notempty() && (thisField.rawValue as String).length() >= minLength }

    Boolean maxlength(Integer maxLength) { return thisField instanceof TextField && notempty() && (thisField.rawValue as String).length() <= maxLength }

    Boolean telnumber() { return regex(TEL_NUMBER_REGEX) }

    Boolean email() { return regex(EMAIL_REGEX) }

}
