package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProcessRole extends Imported {

    public static final String DEFAULT_ROLE = "default";
    public static final String ANONYMOUS_ROLE = "anonymous";

    @Id
    private ObjectId id;
    private I18nString name;
    private String netId;
    private String description;
    private Map<EventType, Event> events;

    public ProcessRole(ObjectId id) {
        this.id = id;
        this.events = new HashMap<>();
    }

    public ProcessRole() {
        this(new ObjectId());
    }

    public ProcessRole(String id) {
        this(new ObjectId(id));
    }

    @EqualsAndHashCode.Include
    public String getStringId() {
        return id.toString();
    }

    public void setStringId(String id) {
        this.id = new ObjectId(id);
    }

    public String getLocalisedName(Locale locale) {
        if (name == null) {
            return null;
        }
        return name.getTranslation(locale);
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

    public void addEvent(Event event) {
        this.events.put(event.getType(), event);
    }

    @Override
    public String toString() {
        return name.getDefaultValue();
    }

    @Override
    public ProcessRole clone() {
        ProcessRole clone = new ProcessRole();
        clone.setStringId(this.getStringId());
        clone.setImportId(this.importId);
        clone.setName(this.name == null ? null : this.name.clone());
        clone.setNetId(this.netId);
        clone.setDescription(this.description);
        return clone;
    }
}