package com.netgrif.workflow.workflow.domain.triggers;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public abstract class TimeTrigger extends Trigger {

    @Getter
    @Setter
    protected LocalDateTime startDate;

    @Getter
    protected String timeString;

    protected TimeTrigger(String timeString) {
        super();
        this.timeString = timeString;
    }

    @Override
    public Trigger clone() {
        return new DelayTimeTrigger(timeString);
    }
}