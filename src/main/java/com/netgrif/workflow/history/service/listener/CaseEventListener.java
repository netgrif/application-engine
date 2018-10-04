package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.usecase.CreateCaseEvent;
import com.netgrif.workflow.event.events.usecase.DeleteCaseEvent;
import com.netgrif.workflow.event.events.usecase.SaveCaseDataEvent;
import com.netgrif.workflow.event.events.usecase.UpdateMarkingEvent;
import com.netgrif.workflow.history.domain.CaseEventLog;
import com.netgrif.workflow.history.domain.CaseSaveDataEventLog;
import com.netgrif.workflow.history.domain.repository.CaseSaveDataEventLogRepository;
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
    public void onUpdateMarkingEvent(UpdateMarkingEvent event) {
        CaseEventLog log = new CaseEventLog(event.getCase());
        log.setMessage(event.getMessage());
        repository.save(log);
    }

    @Autowired
    private CaseSaveDataEventLogRepository caseSaveDataEventLogRepository;

    @EventListener
    public void onSaveCaseDataEvent(SaveCaseDataEvent event) {
        CaseSaveDataEventLog log = new CaseSaveDataEventLog(event.getCase(), event.getData(), event.getUserData());
        log.setMessage(event.getMessage());
        caseSaveDataEventLogRepository.save(log);
    }
}