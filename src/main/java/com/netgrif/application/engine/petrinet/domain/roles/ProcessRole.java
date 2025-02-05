//package com.netgrif.application.engine.petrinet.domain.roles;
//
//import com.netgrif.core.petrinet.domain.I18nString;
//import com.netgrif.application.engine.petrinet.domain.Imported;
//import com.netgrif.core.petrinet.domain.dataset.logic.action.Action;
//import com.netgrif.core.petrinet.domain.events.Event;
//import com.netgrif.core.petrinet.domain.events.EventType;
//import com.netgrif.core.workflow.domain.ProcessResourceId;
//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.Setter;
//import org.bson.types.ObjectId;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//@Getter
//@Document
//@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
//public class ProcessRole extends Imported {
//
//    public static final String DEFAULT_ROLE = "default";
//
//    public static final String ANONYMOUS_ROLE = "anonymous";
//
//    public static final String GLOBAL = "global_";
//
//    @Setter
//    private boolean global;
//
//    @Id
//    @Setter
//    private ProcessResourceId _id;
//
//    private I18nString name;
//
//    @Setter
//    private String netId;
//
//    @Setter
//    private String description;
//
//    @Setter
//    private Map<EventType, Event> events;
//
//    public ProcessRole() {
//        if (this.getNetId() == null) {
//            _id = new ProcessResourceId();
//        } else {
//            _id = new ProcessResourceId(new ObjectId(this.getNetId()));
//        }
//    }
//
//    public ProcessRole(String id) {
//        _id = new ProcessResourceId(id);
//    }
//
//    @EqualsAndHashCode.Include
//    public String getStringId() {
//        return _id.toString();
//    }
//
//    public ProcessResourceId get_id() {
//        return _id;
//    }
//
//    public void set_id(ProcessResourceId _id) {
//        this._id = _id;
//    }
//
//    public void set_id(String id) {
//        this._id = new ProcessResourceId(id);
//    }
//
//    public I18nString getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        setName(new I18nString(name));
//    }
//
//    public void setName(I18nString name) {
//        this.name = name;
//    }
//
//    public String getLocalisedName(Locale locale) {
//        if (name == null)
//            return null;
//        return name.getTranslation(locale);
//    }
//
//    public List<Action> getPreAssignActions() {
//        return getPreActions(EventType.ASSIGN);
//    }
//
//    public List<Action> getPostAssignActions() {
//        return getPostActions(EventType.ASSIGN);
//    }
//
//    public List<Action> getPreCancelActions() {
//        return getPreActions(EventType.CANCEL);
//    }
//
//    public List<Action> getPostCancelActions() {
//        return getPostActions(EventType.CANCEL);
//    }
//
//    private List<Action> getPreActions(EventType type) {
//        if (events != null && events.containsKey(type))
//            return events.get(type).getPreActions();
//        return new LinkedList<>();
//    }
//
//    private List<Action> getPostActions(EventType type) {
//        if (events != null && events.containsKey(type))
//            return events.get(type).getPostActions();
//        return new LinkedList<>();
//    }
//
//    @Override
//    public String toString() {
//        return name.getDefaultValue();
//    }
//
//    @Override
//    public ProcessRole clone() {
//        ProcessRole clone = new ProcessRole();
//        clone.set_id(this._id);
//        clone.setImportId(this.importId);
//        clone.setName(this.name == null ? null : this.name.clone());
//        clone.setNetId(this.netId);
//        clone.setDescription(this.description);
//        return clone;
//    }
//}
