package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roleAssignment")
@EqualsAndHashCode(callSuper = true)
public class ProcessRoleAssignment extends RoleAssignment {
    private Session session;

    public ProcessRoleAssignment() {
        this(Session.forever());
    }

    public ProcessRoleAssignment(Session session) {
        this.session = session;
    }
}
