package com.netgrif.application.engine.event.events.user;

import com.netgrif.application.engine.authentication.domain.Identity;
import lombok.Data;

@Data
public class AdminActionEvent extends UserEvent {

    private String code;

    public AdminActionEvent(Identity user, String code) {
        super(user);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return "User " + user.getUsername() + " run following script: " + code;
    }
}
