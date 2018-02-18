package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public abstract class UserTaskEvent extends TaskEvent {

    @Getter
    protected final User user;

    public UserTaskEvent(User user, Task task, Case useCase) {
        super(task, useCase);
        this.user = user;
    }

    public String getEmail() {
        return user.getEmail();
    }
}