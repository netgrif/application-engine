package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.events.user.*;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.userevents.ActorEventLog;
import com.netgrif.application.engine.history.domain.userevents.ActorRemoveRoleEventLog;
import com.netgrif.application.engine.history.domain.userevents.AdminActionEventLog;
import com.netgrif.application.engine.history.domain.userevents.ActorAssignRoleEventLog;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ActorEventListener {

    private final EventLogRepository repository;

    public ActorEventListener(EventLogRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onUserLoginEvent(UserLoginEvent event) {
        // todo 2058
        repository.save(new ActorEventLog(event.getActor().getEmail()));
    }

    @EventListener
    public void onUserLogoutEvent(UserLogoutEvent event) {
        // todo 2058
        repository.save(new ActorEventLog(event.getActor().getEmail()));
    }

    @EventListener
    public void onUserRegistrationEvent(UserRegistrationEvent event) {
        // todo 2058
        repository.save(new ActorEventLog("email"));
    }

    @EventListener
    public void onActorRoleChangeEvent(ActorAssignRoleEvent event) {
        repository.save(new ActorAssignRoleEventLog(event.getActor().getEmail(), event.getRoles()));
    }

    @EventListener
    public void onActorRoleChangeEvent(ActorRemoveRoleEvent event) {
        repository.save(new ActorRemoveRoleEventLog(event.getActor().getEmail(), event.getRoles()));
    }

    @EventListener
    public void onAdminActionEvent(AdminActionEvent event) {
        repository.save(new AdminActionEventLog(event));
    }
}
