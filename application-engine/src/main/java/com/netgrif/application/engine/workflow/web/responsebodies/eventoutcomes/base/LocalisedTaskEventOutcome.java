package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.objects.dto.response.task.TaskDto;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.TaskEventOutcome;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedTaskEventOutcome extends LocalisedCaseEventOutcome {

    private TaskDto taskDto;

    protected LocalisedTaskEventOutcome(TaskEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.taskDto = outcome.getTask() == null ? null : TaskDto.fromTask(outcome.getTask(), locale);
    }

    protected LocalisedTaskEventOutcome(String message, List<LocalisedEventOutcome> outcomes, Locale locale, Case aCase, com.netgrif.application.engine.objects.workflow.domain.Task task) {
        super(message, outcomes, locale, aCase);
        this.taskDto = task == null ? null : TaskDto.fromTask(task, locale);
    }

    public TaskDto getTask() {
        return taskDto;
    }

    public void setTask(TaskDto taskDto) {
        this.taskDto = taskDto;
    }
}
