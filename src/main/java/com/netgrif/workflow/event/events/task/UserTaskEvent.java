package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public abstract class UserTaskEvent extends TaskEvent {

    @Getter
    protected final IUser user;

    public UserTaskEvent(IUser user, Task task, Case useCase) {
        super(task, useCase);
        this.user = user;
    }

    public String getEmail() {
        return user.getEmail();
    }
}