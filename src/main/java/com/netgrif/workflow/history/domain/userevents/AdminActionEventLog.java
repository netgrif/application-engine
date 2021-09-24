package com.netgrif.workflow.history.domain.userevents;

import com.netgrif.workflow.event.events.user.AdminActionEvent;
import com.netgrif.workflow.history.domain.userevents.UserEventLog;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class AdminActionEventLog extends UserEventLog {

    @Getter
    @Setter
    private String code;

    public AdminActionEventLog(AdminActionEvent event) {
        super(event.getUser().getEmail());
        this.code = event.getCode();
    }
}
