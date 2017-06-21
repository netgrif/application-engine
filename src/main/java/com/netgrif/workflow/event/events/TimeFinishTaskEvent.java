package com.netgrif.workflow.event.events;

import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

import java.time.LocalDateTime;

public class TimeFinishTaskEvent extends TaskEvent {

    public TimeFinishTaskEvent(LocalDateTime time, Task task, Case useCase) {
        super(time, task, useCase);
    }

    @Override
    public String getMessage() {
        return "System finished task " + getTask().getTitle() + " of case " + getUseCase().getTitle() + " caused by time trigger set to" + DateUtils.toString((LocalDateTime) source);
    }
}