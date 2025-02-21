package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "assignment")
@EqualsAndHashCode(callSuper = true)
public class ProcessRoleAssignment extends RoleAssignment {
    private Session session;

    public ProcessRoleAssignment() {
        this.session = Session.forever();
    }

    public ProcessRoleAssignment(Session session) {
        this.session = session;
    }
}
