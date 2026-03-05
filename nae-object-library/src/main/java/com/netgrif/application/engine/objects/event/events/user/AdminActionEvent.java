package com.netgrif.application.engine.objects.event.events.user;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminActionEvent extends UserEvent {

    private String code;

    public AdminActionEvent(LoggedUser user, String code) {
        super(user, null);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return "User %s ran following script: %s".formatted(getActor().getUsername() == null ? MISSING_IDENTIFIER : getActor().getUsername(), code);
    }
}
