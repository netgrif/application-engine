package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public class UserFinishTaskEvent extends UserTaskEvent {

    public UserFinishTaskEvent(User user, Task task, Case useCase) {
        super(user, task, useCase);
    }

    @Override
    public String getMessage() {
        return "Užívateľ " + getEmail() + " dokončil úlohu " + getTask().getTitle() + " na prípade " + getUseCase().getTitle();
    }
}