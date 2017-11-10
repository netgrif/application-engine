package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.event.events.Event;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public abstract class TaskEvent extends Event {

    @Getter
    protected final Case useCase;

    public TaskEvent(Task task, Case useCase) {
        super(task);
        this.useCase = useCase;
    }

    public Task getTask() {
        return (Task) source;
    }
}