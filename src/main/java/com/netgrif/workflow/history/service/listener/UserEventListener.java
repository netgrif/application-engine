package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.history.domain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    @Autowired
    private EventLogRepository repository;
//
//    @EventListener
//    public void onUserLoginEvent(UserLoginEvent event) {
//        UserEventLog log = new UserEventLog(event.getUser().getUsername());
//        log.setMessage(event.getMessage());
//        repository.save(log);
//    }
//
//    @EventListener
//    public void onUserLogoutEvent(UserLogoutEvent event) {
//        UserEventLog log = new UserEventLog(event.getUser().getUsername());
//        log.setMessage(event.getMessage());
//        repository.save(log);
//    }
//
//    @EventListener
//    public void onUserRegistrationEvent(UserRegistrationEvent event) {
//        UserEventLog log = new UserEventLog(event.getUser().getUsername());
//        log.setMessage(event.getMessage());
//        repository.save(log);
//    }
//
//    @EventListener
//    public void onUserRoleChangeEvent(UserRoleChangeEvent event) {
//        UserRoleEventLog log = new UserRoleEventLog(event.getUser().getUsername(), event.getRoles());
//        log.setMessage(event.getMessage());
//        repository.save(log);
//    }
}