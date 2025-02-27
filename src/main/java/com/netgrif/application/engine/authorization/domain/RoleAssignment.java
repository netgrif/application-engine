package com.netgrif.application.engine.authorization.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public abstract class RoleAssignment {
    @Id
    protected ObjectId id;
    @Indexed
    protected String userId;
    @Indexed
    protected String roleId;
    protected String roleImportId;

    public RoleAssignment() {
        this.id = new ObjectId();
    }

    public String getStringId() {
        return this.id.toString();
    }
}
