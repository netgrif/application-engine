package com.netgrif.workflow.event.events;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public abstract class TaskEvent extends Event {

    @Getter
    protected final Task task;

    @Getter
    protected final Case useCase;

    public TaskEvent(Object user, Task task, Case useCase) {
        super(user);
        this.task = task;
        this.useCase = useCase;
    }
}