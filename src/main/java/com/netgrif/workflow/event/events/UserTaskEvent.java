package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public abstract class UserTaskEvent extends TaskEvent {

    public UserTaskEvent(User user, Task task, Case useCase) {
        super(user, task, useCase);
    }

    public User getUser() {
        return (User) source;
    }

    public String getEmail() {
        return getUser().getEmail();
    }
}