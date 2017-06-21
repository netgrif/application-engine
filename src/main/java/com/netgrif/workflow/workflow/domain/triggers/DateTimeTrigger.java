package com.netgrif.workflow.workflow.domain.triggers;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Time trigger that will execute task at specified date and time in specified format (e.g. '2011-12-03T10:15:30').
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html#parse-java.lang.CharSequence-">String format</a>
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Dates">ISO-8601</a>
 */
public class DateTimeTrigger extends TimeTrigger {

    /**
     * Creates new DateTimeTrigger from specified string in ISO-8601 date time format (e.g. '2011-12-03T10:15:30').
     * @param dateTime date and time string in specified format
     * @throws DateTimeParseException if the string cannot be parsed into DateTime
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html#parse-java.lang.CharSequence-">String format</a>
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Dates">ISO-8601</a>
     */
    public DateTimeTrigger(String dateTime) throws DateTimeParseException {
        super(dateTime);
        startDate = LocalDateTime.parse(dateTime);
    }
}