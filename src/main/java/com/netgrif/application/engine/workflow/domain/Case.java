package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
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

@Data
@Document
public class Case implements Serializable {

    private static final long serialVersionUID = 3687481049847498422L;

    @Id
    @Setter(AccessLevel.NONE)
    private ObjectId id;
    @LastModifiedDate
    private LocalDateTime lastModified;
    @NotNull
    private ObjectId petriNetObjectId;
    @JsonIgnore
    @Transient
    @QueryType(PropertyType.NONE)
    private Process petriNet;
    @NotNull
    @Indexed
    private String processIdentifier;
    @JsonIgnore
    private Map<String, Integer> activePlaces = new HashMap<>();
    @NotNull
    private String title;
    private String icon;
    private LocalDateTime creationDate;
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
    @Indexed
    private Author author;
    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private Map<String, Integer> consumedTokens = new HashMap<>();
    @Indexed
    private Map<String, TaskPair> tasks = new HashMap<>();
    @JsonIgnore
    private Map<String, Map<ProcessRolePermission, Boolean>> permissions = new HashMap<>();
    private Map<String, String> properties = new HashMap<>();

    private String uriNodeId;

    public Case() {
        id = new ObjectId();
    }

    public Case(Process petriNet) {
        this();
        this.petriNet = petriNet;
        petriNetObjectId = petriNet.getObjectId();
        processIdentifier = petriNet.getIdentifier();
        activePlaces = petriNet.getActivePlaces();
        icon = petriNet.getIcon();

        permissions = petriNet.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey(ProcessRolePermission.DELETE) || role.getValue().containsKey(ProcessRolePermission.VIEW))
                .map(role -> {
                    Map<ProcessRolePermission, Boolean> permissionMap = new HashMap<>();
                    if (role.getValue().containsKey(ProcessRolePermission.DELETE))
                        permissionMap.put(ProcessRolePermission.DELETE, role.getValue().get(ProcessRolePermission.DELETE));
                    if (role.getValue().containsKey(ProcessRolePermission.VIEW)) {
                        permissionMap.put(ProcessRolePermission.VIEW, role.getValue().get(ProcessRolePermission.VIEW));
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

    public String getPetriNetId() {
        return petriNetObjectId.toString();
    }
}