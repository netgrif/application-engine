package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.importer.model.CaseEventType;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Referencable;
import com.netgrif.application.engine.petrinet.domain.arcs.reference.Type;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.events.CaseEvent;
import com.netgrif.application.engine.petrinet.domain.events.ProcessEvent;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Document
@CompoundIndex(name = "cmp-idx-one", def = "{'identifier': 1, 'version.major': -1, 'version.minor': -1, 'version.patch': -1}")
public class PetriNet extends PetriNetObject {

    @Getter
    @Setter
    @Indexed
    private String identifier;

    /**
     * Contains identifiers of super nets. The last element is the closest parent, the first is the furthest parent.
     * */
    @Getter
    @Setter
    private List<PetriNetIdentifier> parentIdentifiers;

    @Getter
    @Setter
    private String uriNodeId;

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
    private Author author;

    @org.springframework.data.mongodb.core.mapping.Field("places")
    @Getter
    @Setter
    private Map<String, Place> places;

    @org.springframework.data.mongodb.core.mapping.Field("transitions")
    @Getter
    @Setter
    private Map<String, Transition> transitions;

    @org.springframework.data.mongodb.core.mapping.Field("arcs")
    @Getter
    @Setter
    // TODO: release/8.0.0 save sorted by execution priority
    private Map<String, List<Arc>> arcs;//todo: import id

    @org.springframework.data.mongodb.core.mapping.Field("dataset")
    @Getter
    @Setter
    private Map<String, Field<?>> dataSet;

    @org.springframework.data.mongodb.core.mapping.Field("roles")
    @DBRef
    @Getter
    @Setter
    private Map<String, ProcessRole> roles;

    @org.springframework.data.mongodb.core.mapping.Field("transactions")
    @Getter
    @Setter
    private Map<String, Transaction> transactions;//todo: import id

    @Getter
    @Setter
    private Map<ProcessEventType, ProcessEvent> processEvents;

    @Getter
    @Setter
    private Map<CaseEventType, CaseEvent> caseEvents;

    @Getter
    @Setter
    private Map<String, Map<ProcessRolePermission, Boolean>> permissions;

    @Getter
    @Setter
    private List<String> negativeViewRoles;

    @Getter
    @Setter
    private Map<String, Map<ProcessRolePermission, Boolean>> userRefs;

    @Getter
    @Setter
    private List<Function> functions;

    @Getter
    @Setter
    private String importXmlPath;

    @Getter
    @Setter
    private Map<String, String> tags;

    public PetriNet() {
        this.id = new ObjectId();
        this.identifier = "Default";
        this.initials = "";
        this.title = new I18nString("");
        this.importId = "";
        this.version = new Version();
        parentIdentifiers = new ArrayList<>();
        defaultCaseName = new I18nString("");
        creationDate = LocalDateTime.now();
        places = new UniqueKeyMap<>();
        transitions = new UniqueKeyMap<>();
        arcs = new UniqueKeyMap<>();
        dataSet = new UniqueKeyMap<>();
        roles = new LinkedHashMap<>();
        negativeViewRoles = new LinkedList<>();
        transactions = new UniqueKeyMap<>();
        processEvents = new LinkedHashMap<>();
        caseEvents = new LinkedHashMap<>();
        permissions = new HashMap<>();
        userRefs = new HashMap<>();
        functions = new LinkedList<>();
        tags = new UniqueKeyMap<>();
    }

    public void addParentIdentifier(PetriNetIdentifier identifier) {
        parentIdentifiers.add(identifier);
    }

    public void addTag(String tagKey, String tagValue) {
        tags.put(tagKey, tagValue);
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

    public void addPermission(String roleId, Map<ProcessRolePermission, Boolean> permissions) {
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

    public void addUserPermission(String usersRefId, Map<ProcessRolePermission, Boolean> permissions) {
        if (this.userRefs.containsKey(usersRefId) && this.userRefs.get(usersRefId) != null) {
            this.userRefs.get(usersRefId).putAll(permissions);
        } else {
            this.userRefs.put(usersRefId, permissions);
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

    public void addDataSetField(Field<?> field) {
        this.dataSet.put(field.getStringId(), field);
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

    public Optional<Field<?>> getField(String id) {
        return Optional.ofNullable(dataSet.get(id));
    }

    public Place getPlace(String id) {
        return places.get(id);
    }

    public Transition getTransition(String id) {
        if ("fake".equals(id)) {
            return new Transition();
        }
        return transitions.get(id);
    }

    public void initializeArcs() {
        arcs.values().forEach(list -> list.forEach(arc -> {
            arc.setSource(getNode(arc.getSourceId()));
            arc.setDestination(getNode(arc.getDestinationId()));
        }));
    }

    public void initializeTokens(Map<String, Integer> activePlaces) {
        places.values().forEach(place -> place.setTokens(activePlaces.getOrDefault(place.getStringId(), 0)));
    }

    public void initializeArcs(DataSet dataSet) {
        arcs.values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc.getReference() != null)
                .forEach(arc -> {
                    String referenceId = arc.getReference().getReference();
                    arc.getReference().setReferencable(getArcReference(referenceId, arc.getReference().getType(), dataSet));
                });
    }

    private Referencable getArcReference(String referenceId, Type type, DataSet dataSet) {
        if (type == Type.PLACE) {
            return places.get(referenceId);
        } else {
            // TODO: release/8.0.0 check if number field?
            return (Referencable) dataSet.get(referenceId);
        }
    }

    public Map<String, Integer> getActivePlaces() {
        return places.values().stream()
                .filter(Place::hasAnyTokens)
                .collect(Collectors.toMap(PetriNetObject::getStringId, Place::getTokens));
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.put(transaction.getStringId(), transaction);
    }

    public Transaction getTransactionByTransition(Transition transition) {
        return transactions.values().stream()
                .filter(transaction ->
                        transaction.getTransitions().contains(transition.getStringId())
                )
                .findAny()
                .orElse(null);
    }

    public List<Field<?>> getImmediateFields() {
        return this.dataSet.values().stream().filter(Field::isImmediate).collect(Collectors.toList());
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
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostCaseActions(CaseEventType type) {
        if (caseEvents.containsKey(type)) {
            return caseEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPreProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPreActions();
        }
        return new LinkedList<>();
    }

    private List<Action> getPostProcessActions(ProcessEventType type) {
        if (processEvents.containsKey(type)) {
            return processEvents.get(type).getPostActions();
        }
        return new LinkedList<>();
    }

    public boolean hasDynamicCaseName() {
        return defaultCaseNameExpression != null;
    }

    @Override
    public String getStringId() {
        return id.toString();
    }

    public PetriNet clone() {
        PetriNet clone = new PetriNet();
        clone.setIdentifier(this.identifier);
        clone.setParentIdentifiers(this.parentIdentifiers.stream().map(PetriNetIdentifier::clone).collect(Collectors.toList()));
        clone.setUriNodeId(this.uriNodeId);
        clone.setInitials(this.initials);
        clone.setTitle(this.title.clone());
        clone.setDefaultRoleEnabled(this.defaultRoleEnabled);
        clone.setDefaultCaseName(this.defaultCaseName == null ? null : this.defaultCaseName.clone());
        clone.setDefaultCaseNameExpression(this.defaultCaseNameExpression == null ? null : this.defaultCaseNameExpression.clone());
        clone.setIcon(this.icon);
        clone.setCreationDate(this.creationDate);
        clone.setVersion(this.version == null ? null : this.version.clone());
        clone.setAuthor(this.author == null ? null : this.author.clone());
        clone.setTransitions(this.transitions == null ? null : this.transitions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, UniqueKeyMap::new)));
        clone.setRoles(this.roles == null ? null : this.roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, LinkedHashMap::new)));
        clone.setTransactions(this.transactions == null ? null : this.transactions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (v1, v2) -> v1, UniqueKeyMap::new)));
        clone.setImportXmlPath(this.importXmlPath);
        clone.setImportId(this.importId);
        clone.setObjectId(this.id);
        clone.setDataSet(this.dataSet.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x,y)->y, UniqueKeyMap::new))
        );
        clone.setPlaces(this.places.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x,y)->y, UniqueKeyMap::new))
        );
        clone.setArcs(this.arcs.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                        .map(Arc::clone)
                        .collect(Collectors.toList()), (x,y)->y, UniqueKeyMap::new))
        );
        clone.initializeArcs();
        clone.setCaseEvents(this.caseEvents == null ? null : this.caseEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setProcessEvents(this.processEvents == null ? null : this.processEvents.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
        clone.setPermissions(this.permissions == null ? null : this.permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        clone.setUserRefs(this.userRefs == null ? null : this.userRefs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
        this.getNegativeViewRoles().forEach(clone::addNegativeViewRole);
        this.getFunctions().forEach(clone::addFunction);
        clone.setTags(new UniqueKeyMap<>(this.tags));
        return clone;
    }
}