package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public abstract class DataEvent extends Event {

    private LoggedUser user;

    public DataEvent(Object source) {
        super(source);
    }

    public DataEvent(Object source, AbstractUser user) {
        super(source);
        this.user = resolveLoggedUser(user);
    }

    public DataEvent(Object source, EventPhase eventPhase, AbstractUser user) {
        super(source, eventPhase);
        this.user = resolveLoggedUser(user);
    }

    /**
     * Converting an {@link AbstractUser} to a {@link LoggedUser} resolves groups, roles and authorities from the
     * database. A user that is already a {@link LoggedUser} is reused so callers can convert once and publish many events.
     */
    private static LoggedUser resolveLoggedUser(AbstractUser user) {
        return user instanceof LoggedUser loggedUser ? loggedUser : ActorTransformer.toLoggedUser(user);
    }

}
