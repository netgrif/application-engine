package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.task.*;
import com.netgrif.workflow.history.domain.TaskEventLog;
import com.netgrif.workflow.history.domain.UserTaskEventLog;
import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventListener {

    @Autowired
    private EventLogRepository repository;

    @EventListener
    public void onCreateTaskEvent(CreateTaskEvent event) {
        TaskEventLog log = new TaskEventLog(event.getTask(), event.getUseCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onTimeFinishTaskEvent(TimeFinishTaskEvent event) {
        TaskEventLog log = new TaskEventLog(event.getTask(), event.getUseCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onUserAssignTaskEvent(UserAssignTaskEvent event) {
        UserTaskEventLog log = new UserTaskEventLog(event.getTask(), event.getUseCase());
        log.setEmail(event.getUser().getEmail());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onUserCancelTaskEvent(UserCancelTaskEvent event) {
        UserTaskEventLog log = new UserTaskEventLog(event.getTask(), event.getUseCase());
        log.setEmail(event.getUser().getEmail());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onUserDelegateTaskEvent(UserDelegateTaskEvent event) {
        UserTaskEventLog log = new UserTaskEventLog(event.getTask(), event.getUseCase());
        log.setEmail(event.getUser().getEmail());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onFinishTaskEvent(UserFinishTaskEvent event) {
        UserTaskEventLog log = new UserTaskEventLog(event.getTask(), event.getUseCase());
        log.setEmail(event.getUser().getEmail());
        log.setMessage(event.getMessage());
        repository.save(log);
    }
}