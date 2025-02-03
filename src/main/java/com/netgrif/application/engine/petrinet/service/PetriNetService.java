package com.netgrif.application.engine.petrinet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.application.engine.configuration.properties.CacheProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.event.events.Event;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeployEvent;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
//import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.adapter.petrinet.domain.PetriNet;
import com.netgrif.adapter.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.events.ProcessEventType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.petrinet.web.responsebodies.*;
//import com.netgrif.application.engine.rules.domain.facts.NetImportedFact;
//import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService.transformToReference;

@Slf4j
@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    protected IProcessRoleService processRoleService;

    @Autowired
    protected PetriNetRepository repository;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected FileStorageConfiguration fileStorageConfiguration;

//    @Autowired
//    protected IRuleEngine ruleEngine;

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected INextGroupService groupService;

    @Autowired
    protected ObjectFactory<Importer> importerObjectFactory;

    @Autowired
    protected FieldActionsRunner actionsRunner;

//    @Autowired(required = false)
//    protected ILdapGroupRefService ldapGroupService;

    @Autowired
    protected IFieldActionsCacheService functionCacheService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected IEventService eventService;

    @Autowired
    protected IHistoryService historyService;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CacheProperties cacheProperties;

    @Resource
    protected IPetriNetService self;

    @Autowired
    protected IElasticPetriNetMappingService petriNetMappingService;

    @Autowired
    protected IUriService uriService;

    protected ApplicationEventPublisher publisher;

    protected IElasticPetriNetService elasticPetriNetService;

    @Autowired
    public void setElasticPetriNetService(IElasticPetriNetService elasticPetriNetService, ApplicationEventPublisher publisher) {
        this.elasticPetriNetService = elasticPetriNetService;
        this.publisher = publisher;
    }

    protected Importer getImporter() {
        return importerObjectFactory.getObject();
    }

    @Override
    public void evictAllCaches() {
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetById()), cacheProperties.getPetriNetById()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetNewest()), cacheProperties.getPetriNetNewest()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetCache()), cacheProperties.getPetriNetCache()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetByIdentifier()), cacheProperties.getPetriNetByIdentifier()).clear();

    }

    public void evictCache(PetriNet net) {
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
    public PetriNet get(ObjectId petriNetId) {
        Optional<PetriNet> optional = repository.findById(petriNetId.toString());
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Petri net with id [" + petriNetId + "] not found");
        }
        return optional.get();
    }

    @Override
    public List<PetriNet> get(Collection<ObjectId> petriNetIds) {
        return petriNetIds.stream().map(id -> self.get(id)).collect(Collectors.toList());
    }

    @Override
    public List<PetriNet> get(List<String> petriNetIds) {
        return self.get(petriNetIds.stream().map(ObjectId::new).collect(Collectors.toList()));
    }

    @Override
    public PetriNet clone(ObjectId petriNetId) {
        return self.get(petriNetId).clone();
    }

    @Override
    @Deprecated
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser author) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, VersionType.valueOf(releaseType.trim().toUpperCase()), author, uriService.getDefault().getStringId());
    }

    @Override
    @Deprecated
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, String releaseType, LoggedUser author, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, VersionType.valueOf(releaseType.trim().toUpperCase()), author, uriNodeId);
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser author) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, author, uriService.getDefault().getStringId());
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser author, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, author, uriService.getDefault().getStringId(), params);
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser author, String uriNodeId) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        return importPetriNet(xmlFile, releaseType, author, uriNodeId, new HashMap<>());
    }

    @Override
    public ImportPetriNetEventOutcome importPetriNet(InputStream xmlFile, VersionType releaseType, LoggedUser author, String uriNodeId, Map<String, String> params) throws IOException, MissingPetriNetMetaDataException, MissingIconKeyException {
        ImportPetriNetEventOutcome outcome = new ImportPetriNetEventOutcome();
        ByteArrayOutputStream xmlCopy = new ByteArrayOutputStream();
        IOUtils.copy(xmlFile, xmlCopy);
        Optional<PetriNet> imported = getImporter().importPetriNet(new ByteArrayInputStream(xmlCopy.toByteArray()));
        if (imported.isEmpty()) {
            return outcome;
        }
        PetriNet net = imported.get();
        net.setUriNodeId(uriNodeId);

        PetriNet existingNet = getNewestVersionByIdentifier(net.getIdentifier());
        if (existingNet != null) {
            net.setVersion(existingNet.getVersion());
            net.incrementVersion(releaseType);
        }
        processRoleService.saveAll(net.getRoles().values());
        net.setAuthor(author.transformToAuthor());
        functionCacheService.cachePetriNetFunctions(net);
        Path savedPath = getImporter().saveNetFile(net, new ByteArrayInputStream(xmlCopy.toByteArray()));
        xmlCopy.close();
        log.info("Petri net " + net.getTitle() + " (" + net.getInitials() + " v" + net.getVersion() + ") imported successfully and saved in a folder: " + savedPath.toString());

        outcome.setOutcomes(eventService.runActions(net.getPreUploadActions(), null, Optional.empty(), params));
//        evaluateRules(net, EventPhase.PRE);
        publisher.publishEvent(new ProcessDeployEvent(outcome, EventPhase.PRE));
        historyService.save(new ImportPetriNetEventLog(null, EventPhase.PRE, net.getObjectId()));
        save(net);
        outcome.setOutcomes(eventService.runActions(net.getPostUploadActions(), null, Optional.empty(), params));
//        evaluateRules(net, EventPhase.POST);
        publisher.publishEvent(new ProcessDeployEvent(outcome, EventPhase.POST));
        historyService.save(new ImportPetriNetEventLog(null, EventPhase.POST, net.getObjectId()));
        addMessageToOutcome(net, ProcessEventType.UPLOAD, outcome);
        outcome.setNet(imported.get());
//        publisher.publishEvent(new ProcessDeployEvent(outcome));
        return outcome;
    }

    private ImportPetriNetEventOutcome addMessageToOutcome(PetriNet net, ProcessEventType type, ImportPetriNetEventOutcome outcome) {
        if (net.getProcessEvents().containsKey(type)) {
            outcome.setMessage(net.getProcessEvents().get(type).getMessage());
        }
        return outcome;
    }

    protected void evaluateRules(Event event) {
        publisher.publishEvent(event);

    }

    @Override
    public Optional<PetriNet> save(PetriNet petriNet) {
        petriNet.initializeArcs();
        this.evictCache(petriNet);
        petriNet = repository.save(petriNet);

        try {
            elasticPetriNetService.indexNow(this.petriNetMappingService.transform(petriNet));
        } catch (Exception e) {
            log.error("Indexing failed [" + petriNet.getStringId() + "]", e);
        }

        return Optional.of(petriNet);
    }

    @Override
    @Cacheable(value = "petriNetById")
    public PetriNet getPetriNet(String id) {
        Optional<PetriNet> net = repository.findById(id);
        if (net.isEmpty()) {
            throw new IllegalArgumentException("No Petri net with id: " + id + " was found.");
        }
        net.get().initializeArcs();
        return net.get();
    }

    @Override
    @Cacheable(value = "petriNetByIdentifier", key = "#identifier+#version.toString()", unless = "#result == null")
    public PetriNet getPetriNet(String identifier, Version version) {
        PetriNet net = repository.findByIdentifierAndVersion(identifier, version);
        if (net == null) {
            return null;
        }
        net.initializeArcs();
        return net;
    }

    @Override
    public List<PetriNet> getByIdentifier(String identifier) {
        List<PetriNet> nets = repository.findAllByIdentifier(identifier);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public List<PetriNet> findAllByUriNodeId(String uriNodeId) {
        List<PetriNet> nets = elasticPetriNetService.findAllByUriNodeId(uriNodeId);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public List<PetriNet> findAllById(List<String> ids) {
        return StreamSupport.stream(repository.findAllById(ids).spliterator(), false).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "petriNetNewest", unless = "#result == null")
    public PetriNet getNewestVersionByIdentifier(String identifier) {
        List<PetriNet> nets = repository.findByIdentifier(identifier, PageRequest.of(0, 1, Sort.Direction.DESC, "version.major", "version.minor", "version.patch")).getContent();
        if (nets.isEmpty()) {
            return null;
        }
        return nets.get(0);
    }

    /**
     * Determines which of the provided Strings are identifiers of {@link PetriNet}s uploaded in the system.
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
                PetriNet.class,
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
        useCase.getPetriNet().getTransitions().forEach((key, value) -> pn.getTransitions().add(new TransitionImportReference(value)));
        useCase.getPetriNet().getPlaces().forEach((key, value) -> pn.getPlaces().add(new PlaceImportReference(value)));
        useCase.getPetriNet().getArcs().forEach((key, arcs) -> {
            arcs.forEach(arc -> pn.getArcs().add(new ArcImportReference(arc)));
        });
        pn.getAssignedTasks().addAll(historyService.findAllAssignTaskEventLogsByCaseId(caseId)
                .stream().map(TaskEventLog::getTransitionId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        pn.getFinishedTasks().addAll(historyService.findAllFinishTaskEventLogsByCaseId(caseId)
                .stream().map(TaskEventLog::getTransitionId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        return pn;
    }

    @Override
    public List<PetriNet> getAll() {
        List<PetriNet> nets = repository.findAll();
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public FileSystemResource getFile(String netId, String title) {
        if (title == null || title.length() == 0) {
            Query query = Query.query(Criteria.where("_id").is(new ObjectId(netId)));
            query.fields().include("_id").include("title");
            List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);
            if (nets.isEmpty())
                return null;
            title = nets.get(0).getTitle().getDefaultValue();
        }
        return new FileSystemResource(fileStorageConfiguration.getStorageArchived() + netId + "-" + title + Importer.FILE_EXTENSION);
    }

    @Override
    public List<PetriNetReference> getReferences(LoggedUser user, Locale locale) {
        return getAll().stream().map(net -> transformToReference(net, locale)).collect(Collectors.toList());
    }

    @Override
    public List<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale) {
        return getByIdentifier(identifier).stream().map(net -> transformToReference(net, locale)).collect(Collectors.toList());
    }

    @Override
    public List<PetriNetReference> getReferencesByVersion(Version version, LoggedUser user, Locale locale) {
        List<PetriNetReference> references;

        if (version == null) {
            GroupOperation groupByIdentifier = Aggregation.group("identifier").max("version").as("version");
            Aggregation aggregation = Aggregation.newAggregation(groupByIdentifier);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "petriNet", Document.class);
            references = results.getMappedResults().stream()
                    .map(doc -> {
                        Document versionDoc = doc.get("version", Document.class);
                        Version refVersion = new Version(versionDoc.getLong("major"), versionDoc.getLong("minor"), versionDoc.getLong("patch"));
                        return getReference(doc.getString("_id"), refVersion, user, locale);
                    })
                    .collect(Collectors.toList());
        } else {
            references = repository.findAllByVersion(version).stream()
                    .map(net -> transformToReference(net, locale)).collect(Collectors.toList());
        }

        return references;
    }

    @Override
    public List<PetriNetReference> getReferencesByUsersProcessRoles(LoggedUser user, Locale locale) {
        Query query = Query.query(getProcessRolesCriteria(user));
        return mongoTemplate.find(query, PetriNet.class).stream().map(net -> transformToReference(net, locale)).collect(Collectors.toList());
    }

    @Override
    public PetriNetReference getReference(String identifier, Version version, LoggedUser user, Locale locale) {
        PetriNet net = version == null ? getNewestVersionByIdentifier(identifier) : getPetriNet(identifier, version);
        return net != null ? transformToReference(net, locale) : new PetriNetReference();
    }

    @Override
    public List<TransitionReference> getTransitionReferences(List<String> netIds, LoggedUser user, Locale locale) {
        Iterable<PetriNet> nets = get(netIds);
        List<TransitionReference> references = new ArrayList<>();

        nets.forEach(net -> references.addAll(net.getTransitions().entrySet().stream()
                .map(entry -> transformToReference(net, entry.getValue(), locale)).collect(Collectors.toList())));

        return references;
    }

    @Override
    public List<DataFieldReference> getDataFieldReferences(List<TransitionReference> transitions, Locale locale) {
        Iterable<PetriNet> nets = repository.findAllById(transitions.stream().map(TransitionReference::getPetriNetId).collect(Collectors.toList()));
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
    public Optional<PetriNet> findByImportId(String id) {
        return Optional.of(repository.findByImportId(id));
    }

    @Override
    public Page<PetriNetReference> search(PetriNetSearch criteriaClass, LoggedUser user, Pageable pageable, Locale locale) {
        Query query = new Query();
        Query queryTotal = new Query();

        if (!user.getSelfOrImpersonated().isAdmin())
            query.addCriteria(getProcessRolesCriteria(user.getSelfOrImpersonated()));

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
                this.addValueCriteria(query, queryTotal, Criteria.where("author.id").is(criteriaClass.getAuthor().getId()));
            }
            if (criteriaClass.getAuthor().getFullName() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.fullName").is(criteriaClass.getAuthor().getFullName()));
            }
        }
        if (criteriaClass.getNegativeViewRoles() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("negativeViewRoles").in(criteriaClass.getNegativeViewRoles()));
        }
        if (criteriaClass.getTags() != null) {
            criteriaClass.getTags().entrySet().forEach(stringStringEntry -> this.addValueCriteria(query, queryTotal, Criteria.where("tags." + stringStringEntry.getKey()).is(stringStringEntry.getValue())));
        }

        query.with(pageable);
        List<PetriNet> nets = mongoTemplate.find(query, PetriNet.class);
        return new PageImpl<>(nets.stream().map(net -> new PetriNetReference(net, locale)).collect(Collectors.toList()), pageable, mongoTemplate.count(queryTotal, PetriNet.class));
    }

    private void addValueCriteria(Query query, Query queryTotal, Criteria criteria) {
        query.addCriteria(criteria);
        queryTotal.addCriteria(criteria);
    }

    @Override
    @Transactional
    public void deletePetriNet(String processId, LoggedUser loggedUser) {
        Optional<PetriNet> petriNetOptional = repository.findById(processId);
        if (!petriNetOptional.isPresent()) {
            throw new IllegalArgumentException("Could not find process with id [" + processId + "]");
        }

        PetriNet petriNet = petriNetOptional.get();
        log.info("[" + processId + "]: Initiating deletion of Petri net " + petriNet.getIdentifier() + " version " + petriNet.getVersion().toString());

        //MODULARISATION: replace PetriNet packages
        //this.userService.removeRoleOfDeletedPetriNet(petriNet, null);
        this.workflowService.deleteInstancesOfPetriNet(petriNet);
        this.processRoleService.deleteRolesOfNet(petriNet, loggedUser);
//        try {
//            ldapGroupService.deleteProcessRoleByPetrinet(petriNet.getStringId());
//        } catch (NullPointerException e) {
//            log.info("LdapGroup and ProcessRole mapping are not activated...");
//        } catch (Exception ex) {
//            log.error("LdapGroup", ex);
//        }


        log.info("[" + processId + "]: User [" + userService.getLoggedOrSystem().getStringId() + "] is deleting Petri net " + petriNet.getIdentifier() + " version " + petriNet.getVersion().toString());
        this.repository.deleteBy_id(petriNet.getObjectId());
        this.evictCache(petriNet);
        // net functions must be removed from cache after it was deleted from repository
        this.functionCacheService.reloadCachedFunctions(petriNet);
        historyService.save(new DeletePetriNetEventLog(null, EventPhase.PRE, petriNet.getObjectId()));
        publisher.publishEvent(new ProcessDeleteEvent(petriNet, EventPhase.POST));
    }

    private Criteria getProcessRolesCriteria(LoggedUser user) {
        return new Criteria().orOperator(user.getProcessRoles().stream()
                .map(role -> Criteria.where("permissions." + role).exists(true)).toArray(Criteria[]::new));
    }

    @Override
    public void runActions(List<Action> actions, PetriNet petriNet) {
        log.info("Running actions of net [" + petriNet.getStringId() + "]");

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
