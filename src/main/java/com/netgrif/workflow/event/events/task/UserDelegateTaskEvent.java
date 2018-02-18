package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class UserDelegateTaskEvent extends UserTaskEvent {

    @Getter
    private User delegated;

    public UserDelegateTaskEvent(User delegate, Task task, Case useCase, User delegated) {
        super(delegate, task, useCase);
        this.delegated = delegated;
    }

    @Override
    public String getMessage() {
        return "User " + getEmail() + " delegated task " + getTask().getTitle() + " of case " + getUseCase().getTitle() + " to user " + delegated.getEmail();
    }
}