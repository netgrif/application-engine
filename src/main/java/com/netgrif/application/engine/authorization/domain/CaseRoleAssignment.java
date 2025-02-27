package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roleAssignment")
@EqualsAndHashCode(callSuper = true)
public class CaseRoleAssignment extends RoleAssignment {
    @Indexed
    private String caseId;
}
