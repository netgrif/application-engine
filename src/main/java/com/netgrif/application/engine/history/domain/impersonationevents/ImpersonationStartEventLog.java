package com.netgrif.application.engine.history.domain.impersonationevents;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class ImpersonationStartEventLog extends ImpersonationEventLog {

    @Getter
    private List<String> roles;

    @Getter
    private List<String> authorities;

    public ImpersonationStartEventLog() {
        super();
    }

    public ImpersonationStartEventLog(String impersonator, String impersonated, List<String> roles, List<String> authorities) {
        super(impersonator, impersonated);
        this.roles = roles;
        this.authorities = authorities;
    }
}
