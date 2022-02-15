package com.netgrif.application.engine.event.publishers;

import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.user.UserLoginEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher extends NaeEventPublisher {

    public UserEventPublisher(ApplicationEventPublisher publisher) {
        super(publisher);
    }

    public void publish(UserLoginEvent event) {
        super.publish(event);
    }
}
