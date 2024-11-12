package com.netgrif.application.engine.event.dispatchers;

import com.netgrif.application.engine.event.events.user.UserEvent;
//import com.netgrif.application.engine.event.services.interfaces.IEventSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

//@Service
//public class UserEventDispatcher extends AbstractDispatcher {
//
//    protected UserEventDispatcher(@Autowired IEventSystemService eventSystemService) {
//        super(eventSystemService);
//    }
//
//    @Override
//    public String getId() {
//        return "userEventDispatcher";
//    }
//
//    @EventListener
//    public void listen(UserEvent object) {
//        super.listen(object);
//    }
//
//    @Override
//    public <T> void listen(Class<T> eventClass, T event) {
//        super.listen(eventClass, event);
//    }
//}
