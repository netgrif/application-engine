package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.model.UserImportModelEvent;
import com.netgrif.workflow.history.domain.ModelEventLog;
import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.jws.WebParam;
import java.io.IOException;

@Component
public class ModelEventListener {

    public static final Logger log = LoggerFactory.getLogger(ModelEventListener.class);

    @Autowired
    private EventLogRepository repository;

    @EventListener
    public void onUserImportModelEvent(UserImportModelEvent event) {
        ModelEventLog eventLog = new ModelEventLog();
        try {
            eventLog.setModel(FileUtil.readAsString(event.getModel()));
        } catch (IOException e) {
            log.error("Setting model event failed: ", e);
        }
        eventLog.setMessage(event.getMessage());
        repository.save(eventLog);
    }
}