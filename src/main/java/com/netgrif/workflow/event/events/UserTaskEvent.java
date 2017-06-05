package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.User;

public class UserTaskEvent extends Event {

    private User user;

    private Activity activityType;

    public UserTaskEvent(Object source, User user, Activity activityType) {
        super(source);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Activity getActivityType() {
        return activityType;
    }

    public void setActivityType(Activity activityType) {
        this.activityType = activityType;
    }

    public String getEmail() {
        return getUser().getEmail();
    }

    public enum Activity {
        ASSIGN,
        FINISH,
        CANCEL,
        DELEGATE
    }
}