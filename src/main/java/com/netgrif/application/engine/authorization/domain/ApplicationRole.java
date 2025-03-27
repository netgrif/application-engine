package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.service.factory.ApplicationRoleAssignmentFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

@Data
@Document(collection = "role")
@EqualsAndHashCode(callSuper = true)
public class ApplicationRole extends Role {

    @Indexed
    private String applicationId;

    public ApplicationRole() {
        this(new ObjectId(), null, null);
    }

    public ApplicationRole(String importId, String applicationId) {
        this(new ObjectId(), importId, applicationId);
    }

    public ApplicationRole(ObjectId id, String importId, String applicationId) {
        super(id);
        this.importId = importId;
        this.events = new HashMap<>(); // application role has no events for now, can be changed in future releases
        this.applicationId = applicationId;
    }


    @Override
    public Class<?> getAssignmentFactoryClass() {
        return ApplicationRoleAssignmentFactory.class;
    }

    @Override
    public String getTitleAsString() {
        return String.format("APPROLE[%s-%s]", applicationId, importId);
    }
}
