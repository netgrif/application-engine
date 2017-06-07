package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;
import lombok.Setter;

public class UserTaskEvent extends Event {

    @Getter @Setter
    private User user;

    @Getter @Setter
    private Task task;

    @Getter @Setter
    private Case useCase;

    @Getter @Setter
    private Activity activityType;

    public UserTaskEvent(Object source, User user, Activity activityType) {
        super(source);
        this.user = user;
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