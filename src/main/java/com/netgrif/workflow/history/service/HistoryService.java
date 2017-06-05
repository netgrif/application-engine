package com.netgrif.workflow.history.service;

import com.netgrif.workflow.event.events.UserTaskEvent;
import com.netgrif.workflow.history.domain.EventLogRepository;
import com.netgrif.workflow.history.domain.UserEventLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class HistoryService implements IHistoryService {

    @Autowired
    private EventLogRepository repository;

    @EventListener(condition = "#event.user != null")
    public void onUserFinishTaskEvent(UserTaskEvent event) {
        UserEventLog log = new UserEventLog();
        log.setEmail(event.getEmail());
        log.setMessage(getMessageByUserActivity(event.getActivityType(), event.getEmail()));
        repository.save(log);
    }

    private String getMessageByUserActivity(UserTaskEvent.Activity activity, String email) {
        switch (activity) {
            case DELEGATE:
                return "";
            case CANCEL:
                return "";
            case ASSIGN:
                return "";
            case FINISH:
                return "User finished task";
            default:
                return "";
        }
    }
}