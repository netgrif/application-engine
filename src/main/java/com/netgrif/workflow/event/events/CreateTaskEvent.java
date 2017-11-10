package com.netgrif.workflow.event.events;

import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public class CreateTaskEvent extends TaskEvent {

    public CreateTaskEvent(Task task, Case useCase) {
        super("System", task, useCase);
    }

    @Override
    public String getMessage() {
        return "Task " + task.getTitle() + " of case " + useCase.getTitle() + " created on " + DateUtils.toString(time);
    }
}