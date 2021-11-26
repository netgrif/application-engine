package com.netgrif.workflow.history.domain.userevents;

import com.netgrif.workflow.event.events.user.AdminActionEvent;
import lombok.Getter;

public class AdminActionEventLog extends UserEventLog {

    @Getter
    private String code;

    public AdminActionEventLog(AdminActionEvent event) {
        super(event.getUser().getEmail());
        this.code = event.getCode();
    }
}
