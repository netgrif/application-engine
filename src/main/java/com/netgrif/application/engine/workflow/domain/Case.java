package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.authentication.domain.Author;
import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.petrinet.domain.PetriNetIdentifier;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
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
    private Process process;
    @NotNull
    @Indexed
    private String processIdentifier;
    /**
     * Contains identifiers of super petri nets. The last element is the closest parent, the first is the furthest parent.
     * */
    private List<PetriNetIdentifier> parentPetriNetIdentifiers;
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
    private AccessPermissions<CasePermission> processRolePermissions = new AccessPermissions<>();
    private AccessPermissions<CasePermission> caseRolePermissions = new AccessPermissions<>();
    private Map<String, String> properties = new HashMap<>();

    private String uriNodeId;

    public Case() {
        id = new ObjectId();
        // TODO: release/8.0.0 spring auditing
        creationDate = LocalDateTime.now();
    }

    public Case(Process petriNet) {
        this();
        this.process = petriNet;
        petriNetObjectId = petriNet.getObjectId();
        processIdentifier = petriNet.getIdentifier();
        parentPetriNetIdentifiers = new ArrayList<>(petriNet.getParentIdentifiers());
        activePlaces = petriNet.getActivePlaces();
        icon = petriNet.getIcon();
        processRolePermissions = new AccessPermissions<>(petriNet.getProcessRolePermissions(), Set.of(CasePermission.CREATE));
        caseRolePermissions = new AccessPermissions<>();
    }

    public String getStringId() {
        return id.toString();
    }

    /**
     * todo javadoc
     * */
    public void addProcessRolePermissions(String roleId, Map<CasePermission, Boolean> permissions) {
        this.processRolePermissions.addPermissions(roleId, permissions);
    }

    /**
     * todo javadoc
     * */
    public void addProcessRolePermissions(AccessPermissions<CasePermission> rolesAndPermissions) {
        this.processRolePermissions.addPermissions(rolesAndPermissions);
    }

    /**
     * todo javadoc
     * */
    public void addCaseRolePermissions(String roleId, Map<CasePermission, Boolean> permissions) {
        this.caseRolePermissions.addPermissions(roleId, permissions);
    }

    /**
     * todo javadoc
     * */
    public void addCaseRolePermissions(AccessPermissions<CasePermission> rolesAndPermissions) {
        this.caseRolePermissions.addPermissions(rolesAndPermissions);
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

    public String getPetriNetId() {
        return petriNetObjectId.toString();
    }
}