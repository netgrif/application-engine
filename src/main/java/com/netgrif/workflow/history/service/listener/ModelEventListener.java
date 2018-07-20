package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModelEventListener {

    @Autowired
    private EventLogRepository repository;

//    @EventListener
//    public void onUserImportModelEvent(UserImportModelEvent event) {
//        ModelEventLog log = new ModelEventLog();
//        try {
//            log.setModel(FileUtil.readAsString(event.getModel()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.setMessage(event.getMessage());
//        repository.save(log);
//    }
}