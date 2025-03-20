package com.netgrif.application.engine.petrinet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.properties.CacheProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.importer.service.ImportResult;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionRunner;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.petrinet.web.responsebodies.*;
import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService.transformToReference;

@Slf4j
@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    private IRoleService roleService;

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    @Autowired
    private IRuleEngine ruleEngine;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private INextGroupService groupService;

    @Autowired
    private Provider<Importer> importerProvider;

    @Autowired
    private ActionRunner actionsRunner;

    @Autowired(required = false)
    private ILdapGroupRefService ldapGroupService;

    @Autowired
    private IFieldActionsCacheService functionCacheService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IEventService eventService;

    @Autowired
    private IHistoryService historyService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheProperties cacheProperties;

    @Resource
    private IPetriNetService self;

    @Autowired
    private IElasticPetriNetMappingService petriNetMappingService;

    @Autowired
    private IUriService uriService;

    private IElasticPetriNetService elasticPetriNetService;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    public void setElasticPetriNetService(IElasticPetriNetService elasticPetriNetService) {
        this.elasticPetriNetService = elasticPetriNetService;
    }

    protected Importer getImporter() {
        return importerProvider.get();
    }

    @Override
    public void evictAllCaches() {
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetById()), cacheProperties.getPetriNetById()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetNewest()), cacheProperties.getPetriNetNewest()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetCache()), cacheProperties.getPetriNetCache()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetByIdentifier()), cacheProperties.getPetriNetByIdentifier()).clear();
    }

    public void evictCache(Process net) {
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetById()), cacheProperties.getPetriNetById()).evict(net.getStringId());
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetNewest()), cacheProperties.getPetriNetNewest()).evict(net.getIdentifier());
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetCache()), cacheProperties.getPetriNetCache()).evict(net.getObjectId());
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetByIdentifier()), cacheProperties.getPetriNetByIdentifier()).evict(net.getIdentifier() + net.getVersion().toString());
    }

    /**
     * Get read only Petri net.
     */
    @Override
    @Cacheable(value = "petriNetCache")
    public Process get(ObjectId petriNetId) {
        Optional<Process> optional = repository.findById(petriNetId.toString());
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Petri net with id [" + petriNetId + "] not found");
        }
        return optional.get();
    }

    @Override
    public List<Process> get(Collection<ObjectId> petriNetIds) {
        return petriNetIds.stream().map(id -> self.get(id)).collect(Collectors.toList());
    }

    @Override
    public List<Process> get(List<String> petriNetIds) {
        return self.get(petriNetIds.stream().map(ObjectId::new).collect(Collectors.toList()));
    }

    @Override
    public Process clone(ObjectId petriNetId) {
        return self.get(petriNetId).clone();
    }

    @Override
    @Deprecated
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, Actor actor) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, VersionType.valueOf(releaseType.trim().toUpperCase()), actor, uriService.getRoot().getStringId());
    }

    @Override
    @Deprecated
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, Actor actor, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, VersionType.valueOf(releaseType.trim().toUpperCase()), actor, uriNodeId);
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, Actor actor) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, actor, uriService.getRoot().getStringId());
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, Actor actor, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, actor, uriService.getRoot().getStringId(), params);
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, Actor actor, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, actor, uriNodeId, new HashMap<>());
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, Actor actor, String uriNodeId, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = new ImportPetriNetEventOutcome();
        ByteArrayOutputStream xmlCopy = new ByteArrayOutputStream();
        IOUtils.copy(xmlFile, xmlCopy);
        ImportResult imported = getImporter().importPetriNet(new ByteArrayInputStream(xmlCopy.toByteArray()));
        if (imported.getProcess() == null) {
            return outcome;
        }
        Process net = imported.getProcess();
        net.setUriNodeId(uriNodeId);

        // TODO: release/8.0.0 fix cacheable
        Process existingNet = getNewestVersionByIdentifier(net.getIdentifier());
        if (existingNet != null) {
            net.setVersion(existingNet.getVersion());
            net.incrementVersion(releaseType);
        }
        roleService.saveAll(imported.getRoles().values());
        net.setAuthorId(actor.getStringId());
        functionCacheService.cachePetriNetFunctions(net);
        // TODO: release/8.0.0
//        Path savedPath = getImporter().saveNetFile(net, new ByteArrayInputStream(xmlCopy.toByteArray()));
        xmlCopy.close();
//        log.info("Petri net {} (v{}) imported successfully and saved in a folder: {}", net.getTitle(), net.getVersion(), savedPath.toString());

        outcome.setOutcomes(eventService.runActions(net.getPreUploadActions(), null, Optional.empty(), params));
        evaluateRules(net, EventPhase.PRE);
        historyService.save(new ImportPetriNetEventLog(null, EventPhase.PRE, net.getObjectId()));
        save(net);
        outcome.setOutcomes(eventService.runActions(net.getPostUploadActions(), null, Optional.empty(), params));
        evaluateRules(net, EventPhase.POST);
        historyService.save(new ImportPetriNetEventLog(null, EventPhase.POST, net.getObjectId()));
        addMessageToOutcome(net, ProcessEventType.UPLOAD, outcome);
        outcome.setNet(net);
        return outcome;
    }

    private ImportPetriNetEventOutcome addMessageToOutcome(Process net, ProcessEventType type, ImportPetriNetEventOutcome outcome) {
        if (net.getProcessEvents().containsKey(type)) {
            outcome.setMessage(net.getProcessEvents().get(type).getMessage());
        }
        return outcome;
    }

    protected void evaluateRules(Process net, EventPhase phase) {
        int rulesExecuted = ruleEngine.evaluateRules(net, new NetImportedFact(net.getStringId(), phase));
        if (rulesExecuted > 0) {
            save(net);
        }
    }

    @Override
    public Optional<Process> save(Process petriNet) {
        petriNet.initializeArcs();
        this.evictCache(petriNet);
        petriNet = repository.save(petriNet);

        try {
            elasticPetriNetService.indexNow(this.petriNetMappingService.transform(petriNet));
        } catch (Exception e) {
            log.error("Indexing failed [{}]", petriNet.getStringId(), e);
        }

        return Optional.of(petriNet);
    }

    @Override
    @Cacheable(value = "petriNetById")
    public Process getPetriNet(String id) {
        Optional<Process> net = repository.findById(id);
        if (net.isEmpty()) {
            throw new IllegalArgumentException("No Petri net with id: " + id + " was found.");
        }
        net.get().initializeArcs();
        return net.get();
    }

    @Override
    @Cacheable(value = "petriNetByIdentifier", key = "#identifier+#version.toString()", unless = "#result == null")
    public Process getPetriNet(String identifier, Version version) {
        Process net = repository.findByIdentifierAndVersion(identifier, version);
        if (net == null) {
            return null;
        }
        net.initializeArcs();
        return net;
    }

    @Override
    public List<Process> getByIdentifier(String identifier) {
        List<Process> nets = repository.findAllByIdentifier(identifier);
        nets.forEach(Process::initializeArcs);
        return nets;
    }

    @Override
    public List<Process> findAllByUriNodeId(String uriNodeId) {
        List<Process> nets = elasticPetriNetService.findAllByUriNodeId(uriNodeId);
        nets.forEach(Process::initializeArcs);
        return nets;
    }

    @Override
    public List<Process> findAllById(List<String> ids) {
        return StreamSupport.stream(repository.findAllById(ids).spliterator(), false).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "petriNetNewest", unless = "#result == null")
    public Process getNewestVersionByIdentifier(String identifier) {
        List<Process> nets = repository.findByIdentifier(identifier, PageRequest.of(0, 1, Sort.Direction.DESC, "version.major", "version.minor", "version.patch")).getContent();
        if (nets.isEmpty()) {
            return null;
        }
        return nets.get(0);
    }

    /**
     * Determines which of the provided Strings are identifiers of {@link Process}s uploaded in the system.
     *
     * @param identifiers a list of Strings that represent potential PetriNet identifiers
     * @return a list containing a subset of the input strings that correspond to identifiers of PetriNets that are present in the system
     */
    @Override
    public List<String> getExistingPetriNetIdentifiersFromIdentifiersList(List<String> identifiers) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("identifier").in(identifiers)),
                Aggregation.group("identifier"),
                Aggregation.project("identifier").and("identifier").previousOperation()
        );
        AggregationResults<?> groupResults = mongoTemplate.aggregate(
                agg,
                Process.class,
                TypeFactory.defaultInstance().constructType(new TypeReference<Map<String, String>>() {
                }).getRawClass()
        );

        List<Map<String, String>> result = (List<Map<String, String>>) groupResults.getMappedResults();
        return result.stream().flatMap(v -> v.values().stream()).collect(Collectors.toList());
    }

    @Override
    public PetriNetImportReference getNetFromCase(String caseId) {
        Case useCase = workflowService.findOne(caseId);
        PetriNetImportReference pn = new PetriNetImportReference();
        useCase.getProcess().getTransitions().forEach((key, value) -> pn.getTransitions().add(new TransitionImportReference(value)));
        useCase.getProcess().getPlaces().forEach((key, value) -> pn.getPlaces().add(new PlaceImportReference(value)));
        // TODO: release/8.0.0 needed?
//        useCase.getProcess().getArcs().forEach((key, arcs) -> {
//            arcs.forEach(arc -> pn.getArcs().add(new ArcImportReference(arc)));
//        });
        pn.getAssignedTasks().addAll(historyService.findAllAssignTaskEventLogsByCaseId(caseId)
                .stream().map(TaskEventLog::getTransitionId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        pn.getFinishedTasks().addAll(historyService.findAllFinishTaskEventLogsByCaseId(caseId)
                .stream().map(TaskEventLog::getTransitionId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        return pn;
    }

    @Override
    public List<Process> getAll() {
        List<Process> nets = repository.findAll();
        nets.forEach(Process::initializeArcs);
        return nets;
    }

    @Override
    public FileSystemResource getFile(String netId, String title) {
        if (title == null || title.isEmpty()) {
            Query query = Query.query(Criteria.where("id").is(new ObjectId(netId)));
            query.fields().include("id").include("title");
            List<Process> nets = mongoTemplate.find(query, Process.class);
            if (nets.isEmpty())
                return null;
            title = nets.get(0).getTitle().getDefaultValue();
        }
        return new FileSystemResource(fileStorageConfiguration.getStorageArchived() + netId + "-" + title + Importer.FILE_EXTENSION);
    }

    @Override
    public List<PetriNetReference> getReferences(Locale locale) {
        return getAll().stream().map(net -> transformToReference(net, locale)).collect(Collectors.toList());
    }

    @Override
    public List<PetriNetReference> getReferencesByIdentifier(String identifier, Locale locale) {
        return getByIdentifier(identifier).stream().map(net -> transformToReference(net, locale)).collect(Collectors.toList());
    }

    @Override
    public List<PetriNetReference> getReferencesByVersion(Version version, Locale locale) {
        List<PetriNetReference> references;

        if (version == null) {
            GroupOperation groupByIdentifier = Aggregation.group("identifier").max("version").as("version");
            Aggregation aggregation = Aggregation.newAggregation(groupByIdentifier);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "petriNet", Document.class);
            references = results.getMappedResults().stream()
                    .map(doc -> {
                        Document versionDoc = doc.get("version", Document.class);
                        Version refVersion = new Version(versionDoc.getLong("major"), versionDoc.getLong("minor"), versionDoc.getLong("patch"));
                        return getReference(doc.getString("_id"), refVersion, locale);
                    })
                    .collect(Collectors.toList());
        } else {
            references = repository.findAllByVersion(version).stream()
                    .map(net -> transformToReference(net, locale)).collect(Collectors.toList());
        }

        return references;
    }

    @Override
    public PetriNetReference getReference(String identifier, Version version, Locale locale) {
        Process net = version == null ? getNewestVersionByIdentifier(identifier) : getPetriNet(identifier, version);
        return net != null ? transformToReference(net, locale) : new PetriNetReference();
    }

    @Override
    public List<TransitionReference> getTransitionReferences(List<String> netIds, Locale locale) {
        Iterable<Process> nets = get(netIds);
        List<TransitionReference> references = new ArrayList<>();

        nets.forEach(net -> references.addAll(net.getTransitions().entrySet().stream()
                .map(entry -> transformToReference(net, entry.getValue(), locale)).collect(Collectors.toList())));

        return references;
    }

    @Override
    public List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale) {
        Iterable<Process> nets = repository.findAllById(transitions.stream().map(TransitionReference::getPetriNetId).collect(Collectors.toList()));
        List<DataFieldReference> dataRefs = new ArrayList<>();
        Map<String, List<TransitionReference>> transitionReferenceMap = transitions.stream()
                .collect(Collectors.groupingBy(TransitionReference::getPetriNetId));

        nets.forEach(net -> transitionReferenceMap.get(net.getStringId())
                .forEach(transition -> {
                    Transition trans;
                    if ((trans = net.getTransition(transition.getStringId())) != null) {
                        dataRefs.addAll(trans.getDataSet().entrySet().stream()
                                .map(entry -> transformToReference(net, trans, net.getDataSet().get(entry.getKey()), locale))
                                .collect(Collectors.toList()));
                    }
                }));

        return dataRefs;
    }

    @Override
    public Optional<Process> findByImportId(String id) {
        return Optional.of(repository.findByImportId(id));
    }

    @Override
    public Page<PetriNetReference> search(PetriNetSearch criteriaClass, Pageable pageable, Locale locale) {
        Query query = new Query();
        Query queryTotal = new Query();

        if (criteriaClass.getIdentifier() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("identifier").regex(criteriaClass.getIdentifier(), "i"));
        }
        if (criteriaClass.getTitle() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("title.defaultValue").regex(criteriaClass.getTitle(), "i"));
        }
        if (criteriaClass.getInitials() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("initials").regex(criteriaClass.getInitials(), "i"));
        }
        if (criteriaClass.getDefaultCaseName() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("defaultCaseName.defaultValue").regex(criteriaClass.getDefaultCaseName(), "i"));
        }
        if (criteriaClass.getGroup() != null) {
            if (criteriaClass.getGroup().size() == 1) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.email").is(this.groupService.getGroupOwnerEmail(criteriaClass.getGroup().get(0))));
            } else {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.email").in(this.groupService.getGroupsOwnerEmails(criteriaClass.getGroup())));
            }
        }
        if (criteriaClass.getVersion() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("version").is(criteriaClass.getVersion()));
        }
        if (criteriaClass.getAuthor() != null) {
            if (criteriaClass.getAuthor().getEmail() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.email").is(criteriaClass.getAuthor().getEmail()));
            }
            if (criteriaClass.getAuthor().getId() != null) {
                // todo 2058 overit ci funguje
                this.addValueCriteria(query, queryTotal, Criteria.where("author.id").is(criteriaClass.getAuthor().getId()));
            }
            if (criteriaClass.getAuthor().getFullName() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.fullName").is(criteriaClass.getAuthor().getFullName()));
            }
        }
        if (criteriaClass.getNegativeViewProcessRoles() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("negativeViewProcessRoles").in(criteriaClass.getNegativeViewProcessRoles()));
        }
        if (criteriaClass.getTags() != null) {
            criteriaClass.getTags().entrySet().forEach(stringStringEntry -> this.addValueCriteria(query, queryTotal, Criteria.where("tags." + stringStringEntry.getKey()).is(stringStringEntry.getValue())));
        }

        query.with(pageable);
        List<Process> nets = mongoTemplate.find(query, Process.class);
        return new PageImpl<>(nets.stream().map(net -> new PetriNetReference(net, locale)).collect(Collectors.toList()), pageable, mongoTemplate.count(queryTotal, Process.class));
    }

    private void addValueCriteria(Query query, Query queryTotal, Criteria criteria) {
        query.addCriteria(criteria);
        queryTotal.addCriteria(criteria);
    }

    @Override
    @Transactional
    public void deletePetriNet(String processId) {
        Optional<Process> petriNetOptional = repository.findById(processId);
        if (petriNetOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find process with id [" + processId + "]");
        }

        Process petriNet = petriNetOptional.get();
        List<Process> childPetriNets = repository.findAllChildrenByParentId(petriNet.getObjectId());

        for (Process childPetriNet : childPetriNets) {
            deletePetriNet(childPetriNet);
        }
        deletePetriNet(petriNet);
    }

    private void deletePetriNet(Process process) {
        log.info("[{}]: Initiating deletion of Petri net {} version {}", process.getStringId(), process.getIdentifier(), process.getVersion().toString());

        this.workflowService.deleteInstancesOfPetriNet(process);
        try {
            ldapGroupService.deleteRoleByPetriNet(process.getStringId());
        } catch (NullPointerException e) {
            log.info("LdapGroup and Role mapping are not activated...");
        } catch (Exception ex) {
            log.error("LdapGroup", ex);
        }

        log.info("[{}]: Actor [{}] is deleting Petri net {} version {}", process.getStringId(),
                identityService.getLoggedIdentity().getActiveActorId(), process.getIdentifier(), process.getVersion().toString());
        this.repository.deleteById(process.getObjectId());
        this.evictCache(process);
        // net functions must be removed from cache after it was deleted from repository
        this.functionCacheService.reloadCachedFunctions(process);
        historyService.save(new DeletePetriNetEventLog(null, EventPhase.PRE, process.getObjectId()));
    }

    @Override
    public void runActions(List<Action> actions, Process petriNet) {
        log.info("Running actions of net [{}]", petriNet.getStringId());

        actions.forEach(action -> {
            actionsRunner.run(action, null, new HashMap<>(), petriNet.getFunctions());
        });
    }

    protected <T> T requireNonNull(T obj, Object... item) {
        if (obj == null) {
            if (item.length > 0) {
                log.error("Null Pointer Exception", item);
            }
            throw new NullPointerException();
        }
        return obj;
    }
}
