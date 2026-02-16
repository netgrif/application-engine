package com.netgrif.application.engine.objects.petrinet.domain.roles;

import com.netgrif.application.engine.objects.annotations.EnsureCollection;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.Imported;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.events.Event;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import com.querydsl.core.annotations.QueryEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;


import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
@EnsureCollection
@QueryEntity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class ProcessRole extends Imported implements Workspaceable {

    public static final String DEFAULT_ROLE = "default";

    public static final String ANONYMOUS_ROLE = "anonymous";

    public static final String GLOBAL = "global_";

    @Setter
    @Indexed
    private boolean global;

    @Getter
    private ProcessResourceId _id;

    @Getter
    private I18nString name;

    @Setter
    @Indexed
    private String processId;

    @Setter
    @Indexed
    private String processIdentifier;

    @Setter
    @Indexed
    private String workspaceId;

    @Setter
    private I18nString processTitle;

    @Setter
    private String description;

    @Setter
    private Map<EventType, Event> events;

    public ProcessRole() {
        if (this.getProcessId() == null) {
            _id = new ProcessResourceId();
        } else {
            _id = new ProcessResourceId(new ObjectId(this.getProcessId()));
        }
    }

    public ProcessRole(ProcessRole processRole) {
        this.global = processRole.global;
        this._id = processRole._id;
        this.name = processRole.name;
        this.processId = processRole.processId;
        this.processIdentifier = processRole.processIdentifier;
        this.workspaceId = processRole.workspaceId;
        this.processTitle = processRole.processTitle;
        this.description = processRole.description;
        this.events = processRole.events;
    }

    public ProcessRole(String id) {
        _id = new ProcessResourceId(id);
    }

    @EqualsAndHashCode.Include
    public String getStringId() {
        return _id.toString();
    }

    public void set_id(ProcessResourceId _id) {
        this._id = _id;
    }

    public void set_id(String id) {
        this._id = new ProcessResourceId(id);
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


}
