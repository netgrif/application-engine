package com.netgrif.application.engine.workflow.domain.triggers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Time trigger that will execute task after specified delay (e.g. 'after 3h 10m') in ISO-8601 format.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">ISO-8601</a>
 */
public class DelayTimeTrigger extends TimeTrigger {

    /**
     * Creates new DelayTimeTrigger from specified string in ISO-8601 duration format PnDTnHnMn.nS
     *
     * @param timeString delay string in format PnDTnHnMn.nS
     * @throws DateTimeParseException if the string cannot be parsed into duration
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">ISO-8601</a>
     */
    public DelayTimeTrigger(String timeString) throws DateTimeParseException {
        super(timeString);
        Duration delayFromNow = Duration.parse(timeString);
        startDate = LocalDateTime.now().plus(delayFromNow);
    }

    @Override
    public DelayTimeTrigger clone() {
        return new DelayTimeTrigger(timeString);
    }
}