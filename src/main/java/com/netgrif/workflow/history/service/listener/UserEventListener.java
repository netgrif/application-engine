package com.netgrif.workflow.history.service.listener;

import com.netgrif.workflow.event.events.user.*;
import com.netgrif.workflow.history.domain.userevents.AdminActionEventLog;
import com.netgrif.workflow.history.domain.userevents.UserEventLog;
import com.netgrif.workflow.history.domain.userevents.UserRoleEventLog;
import com.netgrif.workflow.history.domain.baseevent.repository.EventLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final EventLogRepository repository;

    public UserEventListener(EventLogRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onUserLoginEvent(UserLoginEvent event) {
        UserEventLog log = new UserEventLog(event.getUser().getUsername());
        repository.save(log);
    }

    @EventListener
    public void onUserLogoutEvent(UserLogoutEvent event) {
        UserEventLog log = new UserEventLog(event.getUser().getUsername());
        repository.save(log);
    }

    @EventListener
    public void onUserRegistrationEvent(UserRegistrationEvent event) {
        UserEventLog log = new UserEventLog(event.getUser().getUsername());
        repository.save(log);
    }

    @EventListener
    public void onUserRoleChangeEvent(UserRoleChangeEvent event) {
        UserRoleEventLog log = new UserRoleEventLog(event.getUser().getUsername(), event.getRoles());
        repository.save(log);
    }

    @EventListener
    public void onAdminActionEvent(AdminActionEvent event) {
        AdminActionEventLog log = new AdminActionEventLog(event);
        repository.save(log);
    }
}