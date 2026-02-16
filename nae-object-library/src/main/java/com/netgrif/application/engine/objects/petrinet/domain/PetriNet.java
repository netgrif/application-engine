package com.netgrif.application.engine.objects.petrinet.domain;

import com.netgrif.application.engine.objects.annotations.EnsureCollection;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.reference.Referencable;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.reference.Type;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.objects.petrinet.domain.events.CaseEvent;
import com.netgrif.application.engine.objects.petrinet.domain.events.CaseEventType;
import com.netgrif.application.engine.objects.petrinet.domain.events.ProcessEvent;
import com.netgrif.application.engine.objects.petrinet.domain.events.ProcessEventType;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.objects.utils.CopyConstructorUtil;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@EnsureCollection
@QueryEntity
public abstract class PetriNet extends PetriNetObject implements Workspaceable {


    @Getter
    @Setter
    private String identifier; // todo: combination of identifier and version must be unique ... maybe use @CompoundIndex?

    @Getter
    @Setter
    private String uriNodeId;

    @Getter
    @Setter
    private String uri;

    @Getter
    @Setter // todo 2072 workspaceId regex validation
    private String workspaceId;

    @Getter
    private I18nString title;

    @Getter
    @Setter
    private boolean defaultRoleEnabled;

    @Getter
    @Setter
    private boolean anonymousRoleEnabled;

    @Getter
    @Setter
    private I18nString defaultCaseName;

    @Getter
    @Setter
    private Expression defaultCaseNameExpression;

    @Getter
    @Setter
    @Indexed
    private String initials;

    @Getter
    @Setter
    private String icon;

    // TODO: 18. 3. 2017 replace with Spring auditing
    @Getter
    @Setter
    private LocalDateTime creationDate;

    @Getter
    @Setter
    private Version version;

    @Getter
    @Setter
    @Indexed
    private boolean defaultVersion;

    @Getter
    @Setter
    private ActorRef author;

    @Getter
    @Setter
    private LinkedHashMap<String, Place> places;

    @Getter
    @Setter
    private LinkedHashMap<String, Transition> transitions;

    @Getter
    @Setter
    private LinkedHashMap<String, List<Arc>> arcs;//todo: import id

    @Getter
    @Setter
    private LinkedHashMap<String, Field> dataSet;

    @Getter
    @Setter
    private LinkedHashMap<String, ProcessRole> roles;

    @Getter
    @Setter
    private LinkedHashMap<String, Transaction> transactions;//todo: import id

    @Getter
    @Setter
    private Map<ProcessEventType, ProcessEvent> processEvents;

    @Getter
    @Setter
    private Map<CaseEventType, CaseEvent> caseEvents;

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> permissions;

    @Getter
    @Setter
    private List<String> negativeViewRoles;

    @Getter
    @Setter
    private Map<String, Map<String, Boolean>> actorRefs;

    @Getter
    @Setter
    private List<Function> functions;

    private boolean initialized;

    @Getter
    @Setter
    private String importXmlPath;

    @Getter
    @Setter
    private Map<String, String> tags;

    @Getter
    @Setter
    private Set<String> pluginDependencies;

    public PetriNet() {
        this._id = new ObjectId();
        this.identifier = "Default";
        this.initials = "";
        this.title = new I18nString("");
        this.importId = "";
        this.version = new Version();
        this.defaultCaseName = new I18nString("");
        this.initialized = false;
        this.creationDate = LocalDateTime.now();
        this.places = new LinkedHashMap<>();
        this.transitions = new LinkedHashMap<>();
        this.arcs = new LinkedHashMap<>();
        this.dataSet = new LinkedHashMap<>();
        this.roles = new LinkedHashMap<>();
        this.negativeViewRoles = new LinkedList<>();
        this.transactions = new LinkedHashMap<>();
        this.processEvents = new LinkedHashMap<>();
        this.caseEvents = new LinkedHashMap<>();
        this.permissions = new HashMap<>();
        this.actorRefs = new HashMap<>();
        this.functions = new LinkedList<>();
        this.tags = new HashMap<>();
        this.pluginDependencies = new HashSet<>();
        this.makeNonDefault();
    }

    public PetriNet(PetriNet petriNet) {
        this();
        this._id = petriNet.getObjectId();
        this.identifier = petriNet.getIdentifier();
        this.uriNodeId = petriNet.getUriNodeId();
        this.uri = petriNet.getUri();
        this.workspaceId = petriNet.getWorkspaceId();
        this.title = petriNet.getTitle();
        this.importId = petriNet.getImportId();
        this.version = petriNet.getVersion();
        this.defaultVersion = petriNet.isDefaultVersion();
        this.defaultCaseName = petriNet.getDefaultCaseName();
        this.defaultCaseNameExpression = petriNet.getDefaultCaseNameExpression();
        this.initials = petriNet.getInitials();
        this.icon = petriNet.getIcon();
        this.creationDate = petriNet.getCreationDate();
        this.places = petriNet.getPlaces().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new Place(e.getValue()), (x, y) -> y, LinkedHashMap::new));
        this.transitions = petriNet.getTransitions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new Transition(e.getValue()), (x, y) -> y, LinkedHashMap::new));
        this.arcs = petriNet.getArcs().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(arc -> CopyConstructorUtil.copy(arc.getClass(), arc)).collect(Collectors.toList()), (x, y) -> y, LinkedHashMap::new));
        this.dataSet = petriNet.getDataSet().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y) -> y, LinkedHashMap::new));
        this.roles = petriNet.getRoles();
        this.negativeViewRoles = petriNet.getNegativeViewRoles();
        this.transactions = petriNet.getTransactions();
        this.processEvents = petriNet.getProcessEvents();
        this.caseEvents = petriNet.getCaseEvents();
        this.permissions = petriNet.getPermissions();
        this.actorRefs = petriNet.getActorRefs();
        this.functions.addAll(petriNet.getFunctions());
        this.tags = petriNet.getTags();
        this.initialized = true;
        this.importXmlPath = petriNet.getImportXmlPath();
        this.defaultRoleEnabled = petriNet.isDefaultRoleEnabled();
        this.anonymousRoleEnabled = petriNet.isAnonymousRoleEnabled();
        this.author = petriNet.getAuthor();
        Set<String> sourcePlugins = petriNet.getPluginDependencies();
        this.pluginDependencies = sourcePlugins != null
                ? new HashSet<>(sourcePlugins)
                : new HashSet<>();
        initializeArcs();
    }

    public void addPlace(Place place) {
        this.places.put(place.getStringId(), place);
    }

    public void addTransition(Transition transition) {
        this.transitions.put(transition.getStringId(), transition);
    }

    public void addRole(ProcessRole role) {
        this.roles.put(role.getStringId(), role);
    }

    public void addPermission(String roleId, Map<String, Boolean> permissions) {
        if (this.permissions.containsKey(roleId) && this.permissions.get(roleId) != null) {
            this.permissions.get(roleId).putAll(permissions);
        } else {
            this.permissions.put(roleId, permissions);
        }
    }

    public void addNegativeViewRole(String roleId) {
        negativeViewRoles.add(roleId);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void addActorPermission(String actorFieldId, Map<String, Boolean> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        if (this.actorRefs.containsKey(actorFieldId) && this.actorRefs.get(actorFieldId) != null) {
            this.actorRefs.get(actorFieldId).putAll(permissions);
        } else {
            this.actorRefs.put(actorFieldId, permissions);
        }
    }

    public List<Arc> getArcsOfTransition(Transition transition) {
        return getArcsOfTransition(transition.getStringId());
    }

    public List<Arc> getArcsOfTransition(String transitionId) {
        if (arcs.containsKey(transitionId)) {
            return arcs.get(transitionId);
        }
        return new LinkedList<>();
    }

    public void addDataSetField(Field field) {
        this.dataSet.put(field.getStringId(), field);
    }

    public boolean isNotInitialized() {
        return !initialized;
    }

    public void addArc(Arc arc) {
        String transitionId = arc.getTransition().getStringId();
        if (arcs.containsKey(transitionId)) {
            arcs.get(transitionId).add(arc);
        } else {
            List<Arc> arcList = new LinkedList<>();
            arcList.add(arc);
            arcs.put(transitionId, arcList);
        }
    }

    public Node getNode(String importId) {
        if (places.containsKey(importId)) {
            return getPlace(importId);
        }
        if (transitions.containsKey(importId)) {
            return getTransition(importId);
        }
        return null;
    }

    public Optional<Field> getField(String id) {
        return Optional.ofNullable(dataSet.get(id));
    }

    public Place getPlace(String id) {
        return places.get(id);
    }

    public Transition getTransition(String id) {
        return transitions.get(id);
    }

    public void initializeArcs() {
        arcs.values().forEach(list -> list.forEach(arc -> {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
        }));
        initialized = true;
    }

    public void initializeTokens(Map<String, Integer> activePlaces) {
        places.values().forEach(place -> place.setTokens(activePlaces.getOrDefault(place.getStringId(), 0)));
    }

    public void initializeArcs(Map<String, DataField> dataSet) {
        arcs.values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc.getReference() != null)
                .forEach(arc -> {
                    String referenceId = arc.getReference().getReference();
                    arc.getReference().setReferencable(getArcReference(referenceId, arc.getReference().getType(), dataSet));
                });
    }

    private Referencable getArcReference(String referenceId, Type type, Map<String, DataField> dataSet) {
        if (type == Type.PLACE) {
            return places.get(referenceId);
        } else {
            return dataSet.get(referenceId);
        }
    }

    public Map<String, Integer> getActivePlaces() {
        Map<String, Integer> activePlaces = new HashMap<>();
        for (Place place : places.values()) {
            if (place.getTokens() > 0) {
                activePlaces.put(place.getStringId(), place.getTokens());
            }
        }
        return activePlaces;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.put(transaction.getStringId(), transaction);
    }

    public Transaction getTransactionByTransition(Transition transition) {
        return transactions.values().stream()
                .filter(transaction ->
                        transaction.getTransitions().contains(transition.getStringId())
                ).findAny().orElse(null);
    }

    public List<Field> getImmediateFields() {
        return this.dataSet.values().stream().filter(Field::isImmediate).collect(Collectors.toList());
    }

    public boolean isDisplayableInAnyTransition(String fieldId) {
        return transitions.values().stream().parallel().anyMatch(trans -> trans.isDisplayable(fieldId));
    }

    public void incrementVersion(VersionType type) {
        this.version.increment(type);
    }

    @Override
    public String toString() {
        return title.toString();
    }

    public void setTitle(I18nString title) {
        this.title = title;
    }

    public void setTitle(String title) {
        setTitle(new I18nString(title));
    }

    public String getTranslatedDefaultCaseName(Locale locale) {
        if (defaultCaseName == null) {
            return "";
        }
        return defaultCaseName.getTranslation(locale);
    }

    public String getTranslatedTitle(Locale locale) {
        if (title == null) {
            return "";
        }
        return title.getTranslation(locale);
    }

    public List<Function> getFunctions(FunctionScope scope) {
        return functions.stream().filter(function -> function.getScope().equals(scope)).collect(Collectors.toList());
    }

    public List<Action> getPreCreateActions() {
        return getPreCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPostCreateActions() {
        return getPostCaseActions(CaseEventType.CREATE);
    }

    public List<Action> getPreDeleteActions() {
        return getPreCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPostDeleteActions() {
        return getPostCaseActions(CaseEventType.DELETE);
    }

    public List<Action> getPreUploadActions() {
        return getPreProcessActions(ProcessEventType.UPLOAD);
    }

    public List<Action> getPostUploadActions() {
        return getPostProcessActions(ProcessEventType.UPLOAD);
    }

    private List<Action> getPreCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type))
            return caseEvents.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type))
            return caseEvents.get(type).getPostActions();
        return new LinkedList<>();
    }

    private List<Action> getPreProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type))
            return processEvents.get(type).getPreActions();
        return new LinkedList<>();
    }

    private List<Action> getPostProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type))
            return processEvents.get(type).getPostActions();
        return new LinkedList<>();
    }

    public boolean hasDynamicCaseName() {
        return defaultCaseNameExpression != null;
    }

    public void makeDefault() {
        this.setDefaultVersion(true);
    }

    public void makeNonDefault() {
        this.setDefaultVersion(false);
    }

    @Override
    public String getStringId() {
        return _id.toString();
    }
}