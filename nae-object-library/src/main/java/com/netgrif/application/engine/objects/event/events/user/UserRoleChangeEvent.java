package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.Getter;

import java.util.Collection;

public class UserRoleChangeEvent extends UserEvent {

    @Getter
    protected final Collection<ProcessRole> roles;

    public UserRoleChangeEvent(LoggedUser user, Collection<ProcessRole> roles) {
        super(user, null);
        this.roles = roles;
    }

    public UserRoleChangeEvent(AbstractUser user, Collection<ProcessRole> roles) {
        this(ActorTransformer.toLoggedUser(user), roles);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Roles ");
        roles.forEach(role -> {
            sb.append(role.getName());
            sb.append(",");
        });
        sb.append(" assigned to user ");
        sb.append(user.getUsername());
        return sb.toString();
    }
}
