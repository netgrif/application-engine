package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.petrinet.domain.Imported;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public abstract class Role extends Imported {
    // TODO: release/8.0.0 indexed import id
    @Id
    protected ObjectId id;

    public Role(ObjectId id) {
        this.id = id;
    }

    public Role() {
        this(new ObjectId());
    }

    public String getStringId() {
        return this.id.toString();
    }
}
