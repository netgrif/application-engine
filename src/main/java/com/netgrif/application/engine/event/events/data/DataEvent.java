package com.netgrif.application.engine.event.events.data;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import lombok.Getter;

@Getter
public abstract class DataEvent extends Event {

    private LoggedUser user;

    public DataEvent(Object source) {
        super(source);
    }

    public DataEvent(Object source, IUser user) {
        super(source);
        this.user = user.transformToLoggedUser();
    }

    public DataEvent(Object source, EventPhase eventPhase, IUser user) {
        super(source, eventPhase);
        this.user = user.transformToLoggedUser();
    }

}
