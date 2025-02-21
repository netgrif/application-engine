package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public abstract class RoleAssignment {
    @Indexed
    protected String userId;
    @Indexed
    protected String roleId;
    protected String roleImportId;
}
