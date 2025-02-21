package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "role")
@EqualsAndHashCode(callSuper = true)
public class CaseRole extends Role {
    @Indexed
    private String caseId;

    public CaseRole(ObjectId id, String importId, String caseId) {
        super(id);
        this.importId = importId;
        this.caseId = caseId;
    }

    public CaseRole(String importId, String caseId) {
        super(new ObjectId());
        this.importId = importId;
        this.caseId = caseId;
    }

    // todo 2058 copy constructor
}
