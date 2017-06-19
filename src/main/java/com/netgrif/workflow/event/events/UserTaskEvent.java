package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public abstract class UserTaskEvent extends Event {

    @Getter
    private Task task;

    @Getter
    private Case useCase;

    public UserTaskEvent(Object user, Task task, Case useCase) {
        super(user);
        this.task = task;
        this.useCase = useCase;
    }

    public User getUser() {
        return (User) source;
    }

    public String getEmail() {
        return getUser().getEmail();
    }
}