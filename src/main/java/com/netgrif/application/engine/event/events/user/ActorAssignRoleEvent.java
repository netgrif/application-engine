package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.Role;
import lombok.Getter;

import java.util.Collection;

@Getter
public class ActorAssignRoleEvent extends ActorEvent {

    protected final Collection<Role> roles;

    public ActorAssignRoleEvent(Actor actor, Collection<Role> roles) {
        super(actor);
        this.roles = roles;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Roles ");
        roles.forEach(role -> {
            sb.append(role.getTitleAsString());
            sb.append(",");
        });
        sb.append(" assigned to actor ");
        sb.append(actor.getEmail());
        return sb.toString();
    }
}