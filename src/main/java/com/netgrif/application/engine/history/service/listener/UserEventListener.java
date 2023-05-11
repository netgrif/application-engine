package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.events.user.*;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.userevents.AdminActionEventLog;
import com.netgrif.application.engine.history.domain.userevents.UserEventLog;
import com.netgrif.application.engine.history.domain.userevents.UserRoleEventLog;
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
        repository.save(new UserEventLog(event.getUser().getUsername()));
    }

    @EventListener
    public void onUserLogoutEvent(UserLogoutEvent event) {
        repository.save(new UserEventLog(event.getUser().getUsername()));
    }

    @EventListener
    public void onUserRegistrationEvent(UserRegistrationEvent event) {
        repository.save(new UserEventLog(event.getUser().getUsername()));
    }

    @EventListener
    public void onUserRoleChangeEvent(UserRoleChangeEvent event) {
        repository.save(new UserRoleEventLog(event.getUser().getUsername(), event.getRoles()));
    }

    @EventListener
    public void onAdminActionEvent(AdminActionEvent event) {
        repository.save(new AdminActionEventLog(event));
    }
}
