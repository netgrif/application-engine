package com.netgrif.application.engine.rules.service.throwable;

import org.quartz.SchedulerException;

public class RuleEvaluationScheduleException extends Exception {

    public RuleEvaluationScheduleException(SchedulerException e) {
        super(e);
    }
}
