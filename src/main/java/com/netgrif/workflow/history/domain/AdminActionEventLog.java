package com.netgrif.workflow.history.domain;

import com.netgrif.workflow.event.events.user.AdminActionEvent;
import lombok.Data;

@Data
public class AdminActionEventLog extends UserEventLog {

    private String code;

    public AdminActionEventLog(AdminActionEvent event) {
        super(event.getUser().getEmail());
        this.code = event.getCode();
        this.message = event.getMessage();
        this.created = event.getTime();
    }
}
