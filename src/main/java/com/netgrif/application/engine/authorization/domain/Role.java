package com.netgrif.application.engine.authorization.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.importer.model.RoleEventType;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent;
import com.netgrif.application.engine.utils.UniqueKeyMapWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public abstract class Role extends Imported {
    // TODO: release/8.0.0 indexed import id
    @Id
    protected ObjectId id;
    @JsonIgnore
    protected Map<RoleEventType, RoleEvent> events;
    protected UniqueKeyMapWrapper<String> properties;

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

    public List<Action> getPreAssignActions() {
        return getPreActions(EventType.ASSIGN);
    }

    public List<Action> getPostAssignActions() {
        return getPostActions(EventType.ASSIGN);
    }

    public List<Action> getPreCancelActions() {
        return getPreActions(EventType.CANCEL);
    }

    public List<Action> getPostCancelActions() {
        return getPostActions(EventType.CANCEL);
    }

    private List<Action> getPreActions(EventType type) {
        if (events != null && events.containsKey(type))
            return events.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostActions(EventType type) {
        if (events != null && events.containsKey(type))
            return events.get(type).getPostActions();
        return new LinkedList<>();
    }

    public void addEvent(RoleEvent event) {
        this.events.put(event.getType(), event);
    }
}
