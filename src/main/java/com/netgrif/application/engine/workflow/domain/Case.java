package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.arcs.ArcCollection;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.events.CaseEvent;
import com.netgrif.application.engine.workflow.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.workflow.domain.version.Version;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * todo javadoc
 * */
@Data
@Document
public class Case implements Serializable {

    private static final long serialVersionUID = 3687481049847498422L;

    @Id
    @Setter(AccessLevel.NONE)
    private ObjectId id;
    @NotNull
    @Indexed
    private String processIdentifier;
    @Setter(AccessLevel.NONE)
    private Version version;

    @LastModifiedDate
    private LocalDateTime lastModified;
    private LocalDateTime creationDate;
    @NotNull
    private String title;
    private String icon;
    @Indexed
    private Author author;
    private String uriNodeId;
    private Map<String, String> properties = new HashMap<>();

    private UniqueKeyMap<String, Place> places;
    @JsonIgnore
    private Map<String, Integer> activePlaces = new HashMap<>();
    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private Map<String, Integer> consumedTokens = new HashMap<>();

    private UniqueKeyMap<String, ArcCollection> arcs;//todo: import id

    @Indexed
    private Map<String, TaskPair> tasks = new HashMap<>();

    private List<Function> functions;

    private Map<CaseEventType, CaseEvent> caseEvents;
    private Map<ProcessEventType, ProcessEvent> processEvents;

    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private DataSet dataSet = new DataSet();
    /**
     * List of data fields importIds
     */
    @JsonIgnore
    private LinkedHashSet<String> immediateDataFields = new LinkedHashSet<>();
    @Transient
    @QueryType(PropertyType.NONE)
    private List<Field<?>> immediateData = new ArrayList<>();

    @JsonIgnore
    private Map<String, Map<CasePermission, Boolean>> permissions = new HashMap<>();

    /**
     * todo javadoc
     * */
    public Case() {
        id = new ObjectId();
        // TODO: release/8.0.0 spring auditing
        creationDate = LocalDateTime.now();
    }

    /**
     * todo javadoc
     * */
    public Case(Process petriNet) {
        this();
        processIdentifier = petriNet.getIdentifier();
        activePlaces = petriNet.getActivePlaces();
        icon = petriNet.getIcon();

        permissions = petriNet.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey(CasePermission.DELETE) || role.getValue().containsKey(CasePermission.VIEW))
                .map(role -> {
                    Map<CasePermission, Boolean> permissionMap = new HashMap<>();
                    if (role.getValue().containsKey(CasePermission.DELETE))
                        permissionMap.put(CasePermission.DELETE, role.getValue().get(CasePermission.DELETE));
                    if (role.getValue().containsKey(CasePermission.VIEW)) {
                        permissionMap.put(CasePermission.VIEW, role.getValue().get(CasePermission.VIEW));
                    }
                    return new AbstractMap.SimpleEntry<>(role.getKey(), permissionMap);
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public String getStringId() {
        return id.toString();
    }

    public void resolveImmediateDataFields() {
        immediateData = dataSet.getFields().values().stream().filter(Field::isImmediate).collect(Collectors.toList());
        immediateDataFields = immediateData.stream().map(Field::getStringId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public ObjectId getTaskId(String transitionId) {
        if (transitionId == null || !tasks.containsKey(transitionId)) {
            throw new IllegalArgumentException("Case does not have task with transitionId [" + transitionId + "]");
        }
        return tasks.get(transitionId).getTaskId();
    }

    public String getTaskStringId(String transitionId) {
        return getTaskId(transitionId).toString();
    }

    public void addTask(Task task) {
        this.tasks.put(task.getTransitionId(), new TaskPair(task));
    }

    public void removeTasks(List<Task> tasks) {
        tasks.forEach(task ->
                this.tasks.remove(task.getTransitionId())
        );
    }

    public void updateTask(Task task) {
        TaskPair taskPair = tasks.get(task.getTransitionId());
        taskPair.setState(task.getState());
        taskPair.setUserId(task.getAssigneeId());
    }
}