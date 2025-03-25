package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.Role;
import lombok.Getter;

import java.util.Collection;

@Getter
public class ActorRemoveRoleEvent extends ActorEvent {

    protected final Collection<Role> roles;

    public ActorRemoveRoleEvent(Actor actor, Collection<Role> roles) {
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
        sb.append(" removed from actor ");
        sb.append(actor.getEmail());
        return sb.toString();
    }
}
