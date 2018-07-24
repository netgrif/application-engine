package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.model.UserImportModelEvent;
import com.netgrif.workflow.history.domain.ModelEventLog;
import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ModelEventListener {

    @Autowired
    private EventLogRepository repository;

    @EventListener
    public void onUserImportModelEvent(UserImportModelEvent event) {
        ModelEventLog log = new ModelEventLog();
        try {
            log.setModel(FileUtil.readAsString(event.getModel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.setMessage(event.getMessage());
        repository.save(log);
    }
}