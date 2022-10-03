package com.netgrif.application.engine.history.domain.userevents;

import com.netgrif.application.engine.event.events.user.AdminActionEvent;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@TypeAlias("adminActionEventLog")
@Document(collection = "eventLogs")
public class AdminActionEventLog extends UserEventLog {

    @Getter
    private final String code;

    public AdminActionEventLog(AdminActionEvent event) {
        super(event.getUser().getEmail());
        this.code = event.getCode();
    }
}
