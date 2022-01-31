package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.Task;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedTaskEventOutcome extends LocalisedCaseEventOutcome {

    private Task task;

    protected LocalisedTaskEventOutcome(TaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.task = outcome.getTask() == null ? null : new Task(outcome.getTask(), locale);
    }

    protected LocalisedTaskEventOutcome(String message, List<LocalisedEventOutcome> outcomes, Locale locale, Case aCase, com.netgrif.application.engine.workflow.domain.Task task) {
        super(message, outcomes, locale, aCase);
        this.task = task == null ? null : new Task(task, locale);
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
