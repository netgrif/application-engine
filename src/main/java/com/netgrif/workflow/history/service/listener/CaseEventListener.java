package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.usecase.*;
import com.netgrif.workflow.history.domain.CaseEventLog;
import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CaseEventListener {

    @Autowired
    private EventLogRepository repository;

    @EventListener
    public void onCreateCaseEvent(CreateCaseEvent event) {
        CaseEventLog log = new CaseEventLog(event.getCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onDeleteCaseEvent(DeleteCaseEvent event) {
        CaseEventLog log = new CaseEventLog(event.getCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onSaveCaseDataEvent(SaveCaseDataEvent event) {
        CaseEventLog log = new CaseEventLog(event.getCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @EventListener
    public void onUpdateMarkingEvent(UpdateMarkingEvent event) {
        CaseEventLog log = new CaseEventLog(event.getCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }
}