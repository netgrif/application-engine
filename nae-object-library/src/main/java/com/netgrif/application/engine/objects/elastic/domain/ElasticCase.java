package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.utils.CopyConstructorUtil;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.TaskPair;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ElasticCase implements Serializable, Workspaceable {

    @Serial
    private static final long serialVersionUID = 7536959921044863265L;

    private String id;

    private Long version;

    private Long lastModified;

    private String visualId;

    private String processIdentifier;

    private String processId;

    private String workspaceId;

    private String title;

    private LocalDateTime creationDate;

    private Long creationDateSortable;

    private String author;

    private String authorRealm;

    private String authorName;

    private String authorUsername;

    private List<ImmediateField> immediateData;

    private Map<String, DataField> dataSet;

    private Set<String> taskIds;

    private Set<String> taskMongoIds;

    private Set<ElasticTaskPair> tasks;

    private Map<String, Map<String, Boolean>> permissions;

    private Map<String, Map<String, Boolean>> actorRefs;

    private Map<String, Map<String, Boolean>> actors;

    private Set<String> enabledRoles;

    private Set<String> viewRoles;

    private Set<String> viewActorRefs;

    private Set<String> negativeViewRoles;

    private Set<String> viewActors;

    private Set<String> negativeViewActors;

    private Map<String, String> tags;

    public ElasticCase(Case useCase) {
        id = useCase.getStringId();
        lastModified = Timestamp.valueOf(useCase.getLastModified()).getTime();
        processIdentifier = useCase.getProcessIdentifier();
        processId = useCase.getPetriNetId();
        workspaceId = useCase.getWorkspaceId();
        visualId = useCase.getVisualId();
        title = useCase.getTitle();
        creationDate = useCase.getCreationDate().truncatedTo(ChronoUnit.MILLIS);
        creationDateSortable = Timestamp.valueOf(useCase.getCreationDate()).getTime();
        author = useCase.getAuthor().getId();
        authorRealm = useCase.getAuthor().getRealmId();
        authorName = useCase.getAuthor().getFullName();
        authorUsername = useCase.getAuthor().getUsername();
        taskIds = useCase.getTasks() == null ? Collections.emptySet() : useCase.getTasks().stream().map(TaskPair::getTransition).collect(Collectors.toSet());
        taskMongoIds = useCase.getTasks() == null ? Collections.emptySet() : useCase.getTasks().stream().map(TaskPair::getTask).collect(Collectors.toSet());
        tasks = useCase.getTasks() == null ? Collections.emptySet() : useCase.getTasks().stream().map(tp -> new ElasticTaskPair(tp.getTask(), tp.getTransition())).collect(Collectors.toSet());
        enabledRoles = new HashSet<>(useCase.getEnabledRoles());
        viewRoles = new HashSet<>(useCase.getViewRoles());
        viewActorRefs = new HashSet<>(useCase.getViewActorRefs());
        negativeViewRoles = new HashSet<>(useCase.getNegativeViewRoles());
        viewActors = new HashSet<>(useCase.getViewActors());
        negativeViewActors = new HashSet<>(useCase.getNegativeViewActors());
        tags = new HashMap<>(useCase.getTags());
        permissions = deepCopy(useCase.getPermissions());
        actors = deepCopy(useCase.getActors());
        actorRefs = deepCopy(useCase.getActorRefs());
        dataSet = new HashMap<>();
        immediateData = useCase.getImmediateData() == null ? Collections.emptyList() : useCase.getImmediateData().stream().map(ImmediateField::new).collect(Collectors.toList());
    }

    public void update(ElasticCase useCase) {
        version++;
        lastModified = useCase.getLastModified();
        workspaceId = useCase.getWorkspaceId();
        title = useCase.getTitle();
        taskIds = useCase.getTaskIds() == null ? new HashSet<>() : new HashSet<>(useCase.getTaskIds());
        taskMongoIds = useCase.getTaskMongoIds() == null ? new HashSet<>() : new HashSet<>(useCase.getTaskMongoIds());
        tasks = useCase.getTasks() == null ? new HashSet<>() : useCase.getTasks().stream()
                .map(tp -> new ElasticTaskPair(tp.getTask(), tp.getTransition()))
                .collect(Collectors.toSet());
        enabledRoles = useCase.getEnabledRoles() == null ? new HashSet<>() : new HashSet<>(useCase.getEnabledRoles());
        viewRoles = useCase.getViewRoles() == null ? new HashSet<>() : new HashSet<>(useCase.getViewRoles());
        viewActorRefs = useCase.getViewActorRefs() == null ? new HashSet<>() : new HashSet<>(useCase.getViewActorRefs());
        negativeViewRoles = useCase.getNegativeViewRoles() == null ? new HashSet<>() : new HashSet<>(useCase.getNegativeViewRoles());
        viewActors = useCase.getViewActors() == null ? new HashSet<>() : new HashSet<>(useCase.getViewActors());
        negativeViewActors = useCase.getNegativeViewActors() == null ? new HashSet<>() : new HashSet<>(useCase.getNegativeViewActors());
        tags = useCase.getTags() == null ? new HashMap<>() : new HashMap<>(useCase.getTags());
        permissions = deepCopy(useCase.getPermissions());
        actors = deepCopy(useCase.getActors());
        actorRefs = deepCopy(useCase.getActorRefs());
        dataSet = useCase.getDataSet() == null ? new HashMap<>() : useCase.getDataSet().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> CopyConstructorUtil.copy(entry.getValue().getClass(), entry.getValue())));
        immediateData = useCase.getImmediateData() == null ? new ArrayList<>() : useCase.getImmediateData().stream()
                .map(field -> new ImmediateField(field.getStringId(), new I18nString(field.getName()), field.getType()))
                .collect(Collectors.toList());
    }

    private static Map<String, Map<String, Boolean>> deepCopy(Map<String, Map<String, Boolean>> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? new HashMap<>() : new HashMap<>(e.getValue())));
    }
}
