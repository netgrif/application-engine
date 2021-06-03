package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.localised;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.Task;
import lombok.Data;

import java.util.Locale;

@Data
public abstract class LocalisedTaskEventOutcome extends LocalisedEventOutcome {

    private Task task;

    private ChangedFieldsTree data;

    protected LocalisedTaskEventOutcome(TaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.task = new Task(outcome.getTask(), locale);
        this.data = outcome.getData();
    }

    protected LocalisedTaskEventOutcome() {
    }
}
