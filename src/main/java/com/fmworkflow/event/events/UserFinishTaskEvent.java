package com.fmworkflow.event.events;

import com.fmworkflow.auth.domain.User;
import org.springframework.context.ApplicationEvent;

public class UserFinishTaskEvent extends ApplicationEvent {
    private User user;

    public UserFinishTaskEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}