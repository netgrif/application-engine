package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.service.factory.CaseRoleAssignmentFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

@Data
@Document(collection = "role")
@EqualsAndHashCode(callSuper = true)
public class CaseRole extends Role {
    @Indexed
    private String caseId;

    public CaseRole() {
        this(new ObjectId(), null, null);
    }

    public CaseRole(String importId, String caseId) {
        this(new ObjectId(), importId, caseId);
    }

    public CaseRole(ObjectId id, String importId, String caseId) {
        super(id);
        this.importId = importId;
        this.caseId = caseId;
        this.events = new HashMap<>(); // case role has no events for now, can be changed in future releases
    }

    @Override
    public Class<?> getAssignmentFactoryClass() {
        return CaseRoleAssignmentFactory.class;
    }

    @Override
    public String getTitleAsString() {
        return caseId + "-" + importId;
    }
}
