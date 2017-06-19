package com.netgrif.workflow.event.events;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public class UserAssignTaskEvent extends UserTaskEvent{

    public UserAssignTaskEvent(Object user, Task task, Case useCase) {
        super(user, task, useCase);
    }

    @Override
    public String getMessage() {
        return "User " + getEmail() + " assigned task " + getTask().getTitle() + " of case " + getUseCase().getTitle();
    }
}