package com.fmworkflow.workflow.domain;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.ZonedDateTime;
import java.util.Date;

public class TimeTrigger implements Trigger {
    private ZonedDateTime startDate;

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        return Date.from(startDate.toInstant());
    }
}