package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Imported;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.Event;
import com.netgrif.application.engine.petrinet.domain.events.EventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
@Document
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProcessRole extends Imported {

    public static final String DEFAULT_ROLE = "default";

    public static final String ANONYMOUS_ROLE = "anonymous";

    @Id
    @Setter
    private ObjectId _id;

    private I18nString name;

    @Getter
    @Setter
    private String netId;

    @Setter
    private String description;

    @Getter
    @Setter
    private Map<EventType, Event> events;

    public ProcessRole() {
        _id = new ObjectId();
    }

    public ProcessRole(String id) {
        _id = new ObjectId(id);
    }

    @EqualsAndHashCode.Include
    public String getStringId() {
        return _id.toString();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public void set_id(String id) {
        this._id = new ObjectId(id);
    }

    public I18nString getName() {
        return name;
    }

    public void setName(String name) {
        setName(new I18nString(name));
    }

    public void setName(I18nString name) {
        this.name = name;
    }

    public String getLocalisedName(Locale locale) {
        if (name == null)
            return null;
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

    @Override
    public String toString() {
        return name.getDefaultValue();
    }

    @Override
    public ProcessRole clone() {
        ProcessRole clone = new ProcessRole();
        clone.set_id(this._id);
        clone.setImportId(this.importId);
        clone.setName(this.name == null ? null : this.name.clone());
        clone.setNetId(this.netId);
        clone.setDescription(this.description);
        return clone;
    }
}