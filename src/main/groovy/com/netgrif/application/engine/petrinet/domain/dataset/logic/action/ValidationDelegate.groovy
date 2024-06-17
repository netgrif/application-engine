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

    Field<?> thisField

    Closure<Boolean> notempty = { return thisField.rawValue != null }

    // boolean field validations
    Closure<Boolean> requiredtrue = { return thisField instanceof BooleanField && notempty && thisField.rawValue == true }

    // date field validations
    Closure<String> future = { return FUTURE }
    Closure<String> today = { return TODAY }
    Closure<String> past = { return PAST }
    Closure<String> now = { return NOW }

    Closure<Boolean> between = { def from, def to -> // todo: retype everything into localdatetime
        if (thisField !instanceof DateField || thisField !instanceof DateTimeField) {
            return false
        }

        LocalDateTime updateDate_TODAY = LocalDateTime.now()

        def fromDate = from instanceof String && parseStringToLocalDate(from) != null ? parseStringToLocalDate(from) : from
        def toDate = to instanceof String && parseStringToLocalDate(to) != null ? parseStringToLocalDate(to) : to

        if ((fromDate == TODAY || fromDate == NOW) && toDate == FUTURE) {
            if (thisField.rawValue < updateDate_TODAY) {
                return false
            }
        } else if (fromDate == PAST && (toDate == TODAY || toDate == NOW)) {
            if (thisField.rawValue > updateDate_TODAY) {
                return false
            }
        } else if (fromDate == PAST && (toDate instanceof LocalDate)) {
            if (thisField.rawValue > toDate) {
                return false
            }
        } else if (fromDate == TODAY && (toDate instanceof LocalDate)) {
            if (thisField.rawValue < toDate || thisField.rawValue > updateDate_TODAY) {
                return false
            }
        } else if ((fromDate instanceof LocalDate) && toDate == TODAY) {
            if (thisField.rawValue < fromDate || thisField.rawValue > updateDate_TODAY) {
                return false
            }
        } else if (toDate == FUTURE && (fromDate instanceof LocalDate)) {
            if (thisField.rawValue < fromDate) {
                return false
            }
        } else if ((fromDate instanceof LocalDate) && (toDate instanceof LocalDate)) {
            if (thisField.rawValue > toDate || thisField.rawValue < fromDate) {
                return false
            }
        }
        return true
    }

    Closure<Boolean> workday = { return (thisField instanceof DateField || thisField instanceof DateTimeField) && notempty && !thisField.rawValue.dayOfWeek.isWeekend() }


    Closure<Boolean> weekend = { return (thisField instanceof DateField || thisField instanceof DateTimeField) && notempty && thisField.rawValue.dayOfWeek.isWeekend() }

    protected static LocalDateTime parseStringToLocalDate(String stringDate) {
        if (stringDate == null) {
            return null
        }
        List<String> patterns = Arrays.asList("dd.MM.yyyy HH:mm:ss", "")
        try {
            return LocalDate.parse(stringDate, DateTimeFormatter.BASIC_ISO_DATE)
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(stringDate, DateTimeFormatter.ISO_DATE)
            } catch (DateTimeParseException ignored2) {
                for (String pattern : patterns) {
                    try {
                        return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(pattern))
                    } catch (DateTimeParseException | IllegalArgumentException ignored3) {
                    }
                }
            }
        }
        return null
    }

    // number field validations
    Closure<String> inf = { return INF }

    Closure<Boolean> odd = { return thisField instanceof NumberField && notempty && thisField.rawValue % 2 != 0 }

    Closure<Boolean> even = { return thisField instanceof NumberField && notempty && thisField.rawValue % 2 == 0 }

    Closure<Boolean> positive = { return thisField instanceof NumberField && notempty && thisField.rawValue >= 0 }

    Closure<Boolean> negative = { return thisField instanceof NumberField && notempty && thisField.rawValue <= 0 }

    Closure<Boolean> decimal = { return thisField instanceof NumberField && notempty && thisField.rawValue % 1 == 0 }

    Closure<Boolean> inrange = { def from, def to ->
        if (from instanceof String && from.toLowerCase() == INF) {
            from = Double.MIN_VALUE
        }

        if (to instanceof String && to.toLowerCase() == INF) {
            to = Double.MAX_VALUE
        }
        return thisField instanceof NumberField && notempty && thisField.rawValue >= from as Double && thisField.rawValue <= to as Double
    }

    // text field validations
    Closure<Boolean> regex = { String pattern -> return thisField instanceof TextField && notempty && thisField.rawValue ==~ pattern }

    Closure<Boolean> minlength = { Integer minLength -> return thisField instanceof TextField && notempty && (thisField.rawValue as String).length() >= minLength }

    Closure<Boolean> maxlength = { Integer maxLength -> return thisField instanceof TextField && notempty && (thisField.rawValue as String).length() <= maxLength }

    Closure<Boolean> telnumber = { -> return regex(TEL_NUMBER_REGEX) }

    Closure<Boolean> email = { -> return regex(EMAIL_REGEX) }

}
