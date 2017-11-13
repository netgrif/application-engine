package com.netgrif.workflow.history.service;

import com.netgrif.workflow.event.events.task.UserFinishTaskEvent;
import com.netgrif.workflow.history.domain.EventLogRepository;
import com.netgrif.workflow.history.domain.UserEventLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class HistoryEventListener {

    @Autowired
    private EventLogRepository repository;

    @EventListener(condition = "#event.user != null")
    public void onUserFinishTaskEvent(UserFinishTaskEvent event) {
        UserEventLog log = new UserEventLog();
        log.setEmail(event.getEmail());
        log.setMessage(event.getMessage());
        repository.save(log);
    }
}
