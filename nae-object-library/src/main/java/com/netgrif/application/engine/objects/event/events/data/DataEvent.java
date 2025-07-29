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
        this.user = ActorTransformer.toLoggedUser(user);
    }

    public DataEvent(Object source, EventPhase eventPhase, AbstractUser user) {
        super(source, eventPhase);
        this.user = ActorTransformer.toLoggedUser(user);
    }

}
