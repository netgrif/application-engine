package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class UserDelegateTaskEvent extends UserTaskEvent {

    @Getter
    private IUser delegated;

    public UserDelegateTaskEvent(IUser delegate, Task task, Case useCase, IUser delegated) {
        super(delegate, task, useCase);
        this.delegated = delegated;
    }

    @Override
    public String getMessage() {
        return "User " + getEmail() + " delegated task " + getTask().getTitle() + " of case " + getUseCase().getTitle() + " to user " + delegated.getEmail();
    }
}