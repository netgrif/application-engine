package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.importer.model.RoleEventType;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public abstract class Role extends Imported {
    // TODO 2058: release/8.0.0 indexed import id
    @Id
    protected ObjectId id;
    protected Map<RoleEventType, RoleEvent> events;

    /**
     * todo javadoc
     * */
    public abstract Class<?> getAssignmentFactoryClass();

    /**
     * todo javadoc
     * */
    public abstract String getTitleAsString();


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
