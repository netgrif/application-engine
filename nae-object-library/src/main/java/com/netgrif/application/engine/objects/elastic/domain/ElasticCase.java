package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.TaskPair;
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
public abstract class ElasticCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 7536959921044863265L;

    private String id;

    private Long version;

    private Long lastModified;

    private String visualId;

    private String processIdentifier;

    private String processId;

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

    private Map<String, Map<String, Boolean>> userRefs;

    private Map<String, Map<String, Boolean>> users;

    private Set<String> enabledRoles;

    private Set<String> viewRoles;

    private Set<String> viewUserRefs;

    private Set<String> negativeViewRoles;

    private Set<String> viewUsers;

    private Set<String> negativeViewUsers;

    private Map<String, String> tags;

    public ElasticCase(Case useCase) {
        id = useCase.getStringId();
        lastModified = Timestamp.valueOf(useCase.getLastModified()).getTime();
        processIdentifier = useCase.getProcessIdentifier();
        processId = useCase.getPetriNetId();
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
        viewUserRefs = new HashSet<>(useCase.getViewUserRefs());
        negativeViewRoles = new HashSet<>(useCase.getNegativeViewRoles());
        viewUsers = new HashSet<>(useCase.getViewUsers());
        negativeViewUsers = new HashSet<>(useCase.getNegativeViewUsers());
        tags = new HashMap<>(useCase.getTags());
        permissions = deepCopy(useCase.getPermissions());
        users = deepCopy(useCase.getUsers());
        userRefs = deepCopy(useCase.getUserRefs());
        dataSet = new HashMap<>();
        immediateData = useCase.getImmediateData().stream().map(ImmediateField::new).collect(Collectors.toList());
    }

    public void update(ElasticCase useCase) {
        version++;
        lastModified = useCase.getLastModified();
        title = useCase.getTitle();
        taskIds = new HashSet<>(useCase.getTaskIds());
        taskMongoIds = new HashSet<>(useCase.getTaskMongoIds());
        tasks = new HashSet<>(useCase.getTasks());
        enabledRoles = new HashSet<>(useCase.getEnabledRoles());
        viewRoles = new HashSet<>(useCase.getViewRoles());
        viewUserRefs = new HashSet<>(useCase.getViewUserRefs());
        negativeViewRoles = new HashSet<>(useCase.getNegativeViewRoles());
        viewUsers = new HashSet<>(useCase.getViewUsers());
        negativeViewUsers = new HashSet<>(useCase.getNegativeViewUsers());
        tags = new HashMap<>(useCase.getTags());
        permissions = deepCopy(useCase.getPermissions());
        users = deepCopy(useCase.getUsers());
        userRefs = deepCopy(useCase.getUserRefs());
        dataSet = new HashMap<>(useCase.getDataSet());
        immediateData = new ArrayList<>(useCase.getImmediateData());
    }

    private static Map<String, Map<String, Boolean>> deepCopy(Map<String, Map<String, Boolean>> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue())));
    }
}
