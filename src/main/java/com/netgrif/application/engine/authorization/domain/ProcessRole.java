package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.authorization.service.factory.ProcessRoleAssignmentFactory;
import com.netgrif.application.engine.importer.model.EventType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.RoleEvent;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(collection = "role")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProcessRole extends Role {

    public static final String DEFAULT_ROLE = "default";
    public static final String ANONYMOUS_ROLE = "anonymous";

    private I18nString title;
    private I18nString description;
    private UniqueKeyMap<String, String> properties;

    public ProcessRole(ObjectId id, String importId) {
        super(id);
        this.importId = importId;
        this.events = new HashMap<>();
    }

    public ProcessRole(String importId) {
        this(new ObjectId(), importId);
    }

    public ProcessRole() {
        this(new ObjectId(), null);
    }

    @Override
    public Class<?> getAssignmentFactoryClass() {
        return ProcessRoleAssignmentFactory.class;
    }

    @Override
    public String getTitleAsString() {
        return this.title != null ? this.title.getDefaultValue() : this.importId;
    }

    @EqualsAndHashCode.Include
    public String getStringId() {
        return id.toString();
    }

    public void setStringId(String id) {
        this.id = new ObjectId(id);
    }

    public String getLocalisedName(Locale locale) {
        if (title == null) {
            return null;
        }
        return title.getTranslation(locale);
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

    @Override
    public String toString() {
        return title.getDefaultValue();
    }

    @Override
    public ProcessRole clone() {
        ProcessRole clone = new ProcessRole(this.importId);
        clone.setStringId(this.getStringId());
        clone.setTitle(this.title == null ? null : this.title.clone());
        clone.setDescription(this.description == null ? null : this.description.clone());
        return clone;
    }
}