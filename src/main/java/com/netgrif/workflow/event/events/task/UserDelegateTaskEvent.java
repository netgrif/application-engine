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
        return "Užívateľ " + getEmail() + " delegoval úlohu " + getTask().getTitle() + " na prípade " + getUseCase().getTitle() + " užívateľovi " + delegated.getEmail();
    }
}