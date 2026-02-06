package com.netgrif.application.engine.petrinet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.configuration.properties.CacheConfigurationProperties;
import com.netgrif.application.engine.files.minio.StorageConfigurationProperties;
import com.netgrif.application.engine.petrinet.params.DeletePetriNetParams;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.objects.event.events.petrinet.ProcessEvent;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.ArcImportReference;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.objects.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.objects.event.events.petrinet.ProcessDeployEvent;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingIconKeyException;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.petrinet.web.responsebodies.*;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService.transformToReference;

@Slf4j
@Service
public class PetriNetService implements IPetriNetService {

    @Autowired
    protected ProcessRoleService processRoleService;

    @Autowired
    protected PetriNetRepository repository;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected StorageConfigurationProperties fileStorageConfiguration;

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected GroupService groupService;

    @Autowired
    protected ObjectFactory<Importer> importerObjectFactory;

    @Autowired
    protected FieldActionsRunner actionsRunner;

    @Autowired
    protected IFieldActionsCacheService functionCacheService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected IEventService eventService;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CacheConfigurationProperties cacheProperties;

    @Resource
    protected PetriNetService self;

    @Autowired
    protected IElasticPetriNetMappingService petriNetMappingService;

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
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetDefault()), cacheProperties.getPetriNetDefault()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetLatest()), cacheProperties.getPetriNetLatest()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetCache()), cacheProperties.getPetriNetCache()).clear();
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetByIdentifier()), cacheProperties.getPetriNetByIdentifier()).clear();
    }

    public void evictCache(PetriNet net) {
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetById()), cacheProperties.getPetriNetById()).evict(net.getStringId());
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetDefault()), cacheProperties.getPetriNetDefault()).evict(net.getIdentifier());
        requireNonNull(cacheManager.getCache(cacheProperties.getPetriNetLatest()), cacheProperties.getPetriNetLatest()).evict(net.getIdentifier());
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
    @Transactional
    public ImportPetriNetEventOutcome importPetriNet(ImportPetriNetParams importPetriNetParams) throws IOException,
            MissingPetriNetMetaDataException, MissingIconKeyException {
        validateAttributes(importPetriNetParams);

        ImportPetriNetEventOutcome outcome = new ImportPetriNetEventOutcome();
        ByteArrayOutputStream xmlCopy = new ByteArrayOutputStream();
        IOUtils.copy(importPetriNetParams.getXmlFile(), xmlCopy);
        Optional<PetriNet> importedProcess = getImporter().importPetriNet(new ByteArrayInputStream(xmlCopy.toByteArray()));
        if (importedProcess.isEmpty()) {
            return outcome;
        }
        PetriNet newProcess = importedProcess.get();
        PetriNet processToMakeNonDefault = checkAndHandleProcessVersion(newProcess, importPetriNetParams.getReleaseType());

        processRoleService.saveAll(newProcess.getRoles().values());
        newProcess.setAuthor(ActorTransformer.toActorRef(importPetriNetParams.getAuthor()));
        Path savedPath = getImporter().saveNetFile(newProcess, new ByteArrayInputStream(xmlCopy.toByteArray()));
        xmlCopy.close();
        log.info("Petri net {} ({} v{}) imported successfully and saved in a folder: {}", newProcess.getTitle(),
                newProcess.getInitials(), newProcess.getVersion(), savedPath);
        functionCacheService.cacheAllPetriNetFunctions();
        runActionAndPublishEvent(outcome, null, newProcess.getPreUploadActions(), importPetriNetParams.getParams(),
                new ProcessDeployEvent(outcome, EventPhase.PRE));

        if (processToMakeNonDefault != null) {
            doSaveInternal(processToMakeNonDefault);
        }
        Optional<PetriNet> saveProcessOpt = doSaveInternal(newProcess);
        functionCacheService.cachePetriNetFunctions(newProcess);

        runActionAndPublishEvent(outcome, saveProcessOpt.orElseThrow(), newProcess.getPostUploadActions(),
                importPetriNetParams.getParams(), new ProcessDeployEvent(outcome, EventPhase.POST));

        return outcome;
    }

    /**
     * Executes a list of actions associated with a Petri net and publishes a corresponding event after execution.
     *
     * <p>This method is responsible for running the specified actions using the event service, updating the
     * provided {@link ImportPetriNetEventOutcome} instance with the outcomes of the executed actions,
     * setting the processed Petri net, and publishing the given event through the application event publisher.</p>
     *
     * @param outcome     the {@link ImportPetriNetEventOutcome} instance where the results of the action executions are stored
     * @param saveProcess the Petri net that has been processed and potentially saved
     * @param actions     a list of {@link Action}s to be executed
     * @param params      a map of key-value pairs containing execution parameters for the actions
     * @param event       the {@link ProcessEvent} associated with the execution and used for event publication
     */
    protected void runActionAndPublishEvent(ImportPetriNetEventOutcome outcome, PetriNet saveProcess, List<Action> actions, Map<String, String> params, ProcessEvent event) {
        outcome.setOutcomes(eventService.runActions(actions, null, Optional.empty(), params));
        outcome.setNet(saveProcess);
        publisher.publishEvent(event);
    }

    /**
     * Validates the version of an importing process. This method can update 'newProces': initialize version,
     * initialize default version attribute. This method can also update the current default version of the process if needed.
     *
     * <h4>Version initialization logic</h4>
     * <ul>
     *     <li>Uploaded process becomes default only if its version is the highest</li>
     *     <li>Only one process can be default. If the importing process is about to get default, the currently default
     *     process becomes non-default</li>
     *     <li>If no version is provided, it's initialized to 1.0.0 or by the existing highest version incremented
     *     by 'releaseType' input parameter</li>
     * </ul>
     *
     * @param newProcess  A process to be checked and updated
     * @param releaseType requested release type level. It's used for version initialization
     * @return The process, which has been made non-default or null if no process updated
     * @throws IllegalArgumentException if the version already exists
     *
     */
    private PetriNet checkAndHandleProcessVersion(PetriNet newProcess, VersionType releaseType) {
        PetriNet processToMakeNonDefault = null;

        if (newProcess.getVersion() != null && self.getPetriNet(newProcess.getIdentifier(), newProcess.getVersion()) != null) {
            throw new IllegalArgumentException("A process [%s] with such version [%s] already exists"
                    .formatted(newProcess.getIdentifier(), newProcess.getVersion()));
        }
        PetriNet existingLatestProcess = self.getLatestVersionByIdentifier(newProcess.getIdentifier());
        boolean makeNonDefaultCurrentVersion = true;
        if (existingLatestProcess == null && newProcess.getVersion() == null) {
            newProcess.setVersion(new Version());
        } else {
            if (newProcess.getVersion() == null) {
                newProcess.setVersion(existingLatestProcess.getVersion().clone());
                newProcess.incrementVersion(releaseType);
            } else if (existingLatestProcess != null && newProcess.getVersion().isLowerThan(existingLatestProcess.getVersion())) {
                makeNonDefaultCurrentVersion = false;
            }
            if (makeNonDefaultCurrentVersion && existingLatestProcess != null && existingLatestProcess.isDefaultVersion()) {
                existingLatestProcess.makeNonDefault();
                processToMakeNonDefault = existingLatestProcess;
            } else if (makeNonDefaultCurrentVersion) {
                PetriNet existingActiveProcess = self.getDefaultVersionByIdentifier(newProcess.getIdentifier());
                if (existingActiveProcess != null) {
                    existingActiveProcess.makeNonDefault();
                    processToMakeNonDefault = existingActiveProcess;
                }
            }
        }
        if (makeNonDefaultCurrentVersion) {
            newProcess.makeDefault();
        }

        return processToMakeNonDefault;
    }

    @Override
    public Optional<PetriNet> save(PetriNet petriNet) {
        return doSaveInternal(petriNet);
    }

    protected final Optional<PetriNet> doSaveInternal(PetriNet petriNet) {
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
        if (identifier == null || version == null) {
            return null;
        }
        PetriNet net = repository.findByIdentifierAndVersion(identifier, version);
        if (net == null) {
            return null;
        }
        net.initializeArcs();
        return net;
    }

    @Override
    public Page<PetriNet> getByIdentifier(String identifier, Pageable pageable) {
        Page<PetriNet> nets = repository.findByIdentifier(identifier, pageable);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public List<PetriNet> findAllById(List<String> ids) {
        return new ArrayList<>(repository.findAllById(ids));
    }

    @Override
    @Cacheable(value = "petriNetDefault", unless = "#result == null")
    public PetriNet getDefaultVersionByIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }

        List<PetriNet> result = repository.findByIdentifierAndDefaultVersion(identifier, true, PageRequest.of(0, 1)).getContent();
        if (!result.isEmpty()) {
            return result.getFirst();
        }

        return null;
    }

    @Override
    @Cacheable(value = "petriNetLatest", unless = "#result == null")
    public PetriNet getLatestVersionByIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        List<PetriNet> processes = repository.findByIdentifier(identifier, PageRequest.of(0, 1,
                Sort.Direction.DESC, "version.major", "version.minor", "version.patch")).getContent();
        if (processes.isEmpty()) {
            return null;
        }
        return processes.getFirst();
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
                com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet.class,
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
        return pn;
    }

    @Override
    public Page<PetriNet> findAllByRoleId(String roleId, Pageable pageable) {
        Page<PetriNet> nets = repository.findAllByRoleId(roleId, pageable);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public Page<PetriNet> getAll(Pageable pageable) {
        Page<PetriNet> nets = repository.findAll(pageable);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public Page<PetriNet> getAllDefault(Pageable pageable) {
        Page<PetriNet> nets = repository.findAllByDefaultVersionTrue(pageable);
        nets.forEach(PetriNet::initializeArcs);
        return nets;
    }

    @Override
    public FileSystemResource getFile(String netId, String title) {
        if (title == null || title.isEmpty()) {
            Query query = Query.query(Criteria.where("_id").is(new ObjectId(netId)));
            query.fields().include("_id").include("title");
            List<com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet> nets = mongoTemplate.find(query,
                    com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet.class);
            if (nets.isEmpty())
                return null;
            title = nets.getFirst().getTitle().getDefaultValue();
        }
        return new FileSystemResource(fileStorageConfiguration.getArchivedPath() + netId + "-" + title + Importer.FILE_EXTENSION);
    }

    @Override
    public Page<PetriNetReference> getReferences(LoggedUser user, Locale locale, Pageable pageable) {
        return getAll(pageable).map(net -> transformToReference(net, locale));
    }

    @Override
    public Page<PetriNetReference> getReferencesByIdentifier(String identifier, LoggedUser user, Locale locale, Pageable pageable) {
        return getByIdentifier(identifier, pageable).map(net -> transformToReference(net, locale));
    }

    @Override
    public Page<PetriNetReference> getReferencesByVersion(Version version, LoggedUser user, Locale locale, Pageable pageable) {
        Page<PetriNetReference> references;
        if (version == null) {
            GroupOperation groupByIdentifier = Aggregation.group("identifier").max("version").as("version");
            Aggregation aggregation;
            if (pageable == null || pageable.isUnpaged()) {
                aggregation = Aggregation.newAggregation(groupByIdentifier);
            } else {
                aggregation = Aggregation.newAggregation(
                        groupByIdentifier,
                        Aggregation.sort(pageable.getSort()),
                        Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                        Aggregation.limit(pageable.getPageSize())
                );
            }
            List<Document> results = mongoTemplate.aggregate(aggregation, "petriNet", Document.class).getMappedResults();
            List<PetriNetReference> referenceList = results.stream()
                    .map(doc -> {
                        Document versionDoc = doc.get("version", Document.class);
                        Version refVersion = new Version(versionDoc.getLong("major"), versionDoc.getLong("minor"),
                                versionDoc.getLong("patch"));
                        return getReference(doc.getString("_id"), refVersion, user, locale);
                    })
                    .collect(Collectors.toList());
            Aggregation countAggregation = Aggregation.newAggregation(
                    groupByIdentifier,
                    Aggregation.count().as("total")
            );
            AggregationResults<Document> countResults = mongoTemplate.aggregate(
                    countAggregation,
                    "petriNet",
                    Document.class
            );

            Number totalNumber = countResults.getUniqueMappedResult() != null
                    ? countResults.getUniqueMappedResult().get("total", Number.class)
                    : 0;
            long total = totalNumber != null ? totalNumber.longValue() : 0L;

            references = new PageImpl<>(referenceList, pageable, total);
        } else {
            references = repository.findAllByVersion(version, pageable).map(net -> transformToReference(net, locale));
        }
        return references;
    }

    @Override
    public List<PetriNetReference> getReferencesByUsersProcessRoles(LoggedUser user, Locale locale) {
        Query query = Query.query(getProcessRolesCriteria(user));
        return mongoTemplate.find(query, com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet.class).stream()
                .map(net -> transformToReference(net, locale))
                .collect(Collectors.toList());
    }

    @Override
    public PetriNetReference getReference(String identifier, Version version, LoggedUser user, Locale locale) {
        PetriNet net = version == null ? self.getDefaultVersionByIdentifier(identifier) : self.getPetriNet(identifier, version);
        return net != null ? transformToReference(net, locale) : new PetriNetReference();
    }

    @Override
    public List<TransitionReference> getTransitionReferences(List<String> netIds, LoggedUser user, Locale locale) {
        Iterable<PetriNet> nets = get(netIds);
        List<TransitionReference> references = new ArrayList<>();

        nets.forEach(net -> references.addAll(net.getTransitions().values().stream()
                .map(transition -> transformToReference(net, transition, locale)).toList()));

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
                        dataRefs.addAll(trans.getDataSet().keySet().stream()
                                .map(fieldId -> transformToReference(net, trans, net.getDataSet().get(fieldId), locale))
                                .toList());
                    }
                }));

        return dataRefs;
    }

    @Override
    public Optional<PetriNet> findByImportId(String id) {
        return Optional.ofNullable(repository.findByImportId(id));
    }

    @Override
    public Page<PetriNetReference> search(PetriNetSearch criteriaClass, LoggedUser user, Pageable pageable, Locale locale) {
        Query query = new Query();
        Query queryTotal = new Query();

        // TODO: resolve impersonation
        if (!user.isAdmin()) {
            query.addCriteria(getProcessRolesCriteria(user));
        }
//        if (!user.getSelfOrImpersonated().isAdmin())
//            query.addCriteria(getProcessRolesCriteria(user.getSelfOrImpersonated()));

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
                this.addValueCriteria(query, queryTotal, Criteria.where("author.email").is(groupService.getGroupOwnerEmail(criteriaClass.getGroup().get(0))));
            } else {
                // TODO: pagination?
                this.addValueCriteria(query, queryTotal, Criteria.where("author.email").in(groupService.getGroupsOwnerEmails(criteriaClass.getGroup())));
            }
        }
        if (criteriaClass.getVersion() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("version").is(criteriaClass.getVersion()));
        }
        if (criteriaClass.getAuthor() != null) {
            if (criteriaClass.getAuthor().getIdentifier() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.identifier").is(criteriaClass.getAuthor().getIdentifier()));
            }
            if (criteriaClass.getAuthor().getId() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.id").is(criteriaClass.getAuthor().getId()));
            }
            if (criteriaClass.getAuthor().getFullName() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.fullName").is(criteriaClass.getAuthor().getFullName()));
            }
            if (criteriaClass.getAuthor().getRealmId() != null) {
                this.addValueCriteria(query, queryTotal, Criteria.where("author.realmId").is(criteriaClass.getAuthor().getRealmId()));
            }
        }
        if (criteriaClass.getNegativeViewRoles() != null) {
            this.addValueCriteria(query, queryTotal, Criteria.where("negativeViewRoles").in(criteriaClass.getNegativeViewRoles()));
        }
        if (criteriaClass.getTags() != null) {
            criteriaClass.getTags().forEach((tagKey, tagValue) ->
                    this.addValueCriteria(query, queryTotal, Criteria.where("tags." + tagKey).is(tagValue)));
        }

        query.with(pageable);
        List<com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet> nets = mongoTemplate.find(query,
                com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet.class);
        return new PageImpl<>(nets.stream().map(net -> new PetriNetReference(net, locale)).collect(Collectors.toList()),
                pageable, mongoTemplate.count(queryTotal, com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet.class));
    }

    protected void addValueCriteria(Query query, Query queryTotal, Criteria criteria) {
        query.addCriteria(criteria);
        queryTotal.addCriteria(criteria);
    }

    @Override
    @Transactional
    public void deletePetriNet(DeletePetriNetParams deletePetriNetParams) {
        doDeletePetriNet(deletePetriNetParams, false);
    }

    @Override
    public void forceDeletePetriNet(DeletePetriNetParams deletePetriNetParams) {
        doDeletePetriNet(deletePetriNetParams, true);
    }

    protected void doDeletePetriNet(DeletePetriNetParams deletePetriNetParams, boolean force) {
        fillAndValidateAttributes(deletePetriNetParams);

        Optional<PetriNet> petriNetOptional = repository.findById(deletePetriNetParams.getPetriNetId());
        if (petriNetOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find process with id [" + deletePetriNetParams.getPetriNetId() + "]");
        }

        PetriNet petriNet = petriNetOptional.get();
        log.info("[{}]: Initiating deletion of Petri net {} version {}", deletePetriNetParams.getPetriNetId(),
                petriNet.getIdentifier(), petriNet.getVersion().toString());

        workflowService.deleteInstancesOfPetriNet(petriNet, force);
        processRoleService.deleteRolesOfNet(petriNet, deletePetriNetParams.getLoggedUser());

        log.info("[{}]: User [{}] is deleting Petri net {} version {}", deletePetriNetParams.getPetriNetId(),
                deletePetriNetParams.getLoggedUser().getStringId(), petriNet.getIdentifier(), petriNet.getVersion().toString());
        publisher.publishEvent(new ProcessDeleteEvent(petriNet, EventPhase.PRE));
        repository.deleteBy_id(petriNet.getObjectId());
        evictCache(petriNet);
        functionCacheService.removeCachedPetriNetFunctions(petriNet.getIdentifier());
        if (petriNet.isDefaultVersion()) {
            PetriNet processToMakeDefault = self.getLatestVersionByIdentifier(petriNet.getIdentifier());
            if (processToMakeDefault != null) {
                log.debug("The default version was removed. Making the latest version of the process [{}] with id [{}] as default...",
                        processToMakeDefault.getIdentifier(), processToMakeDefault.getStringId());
                processToMakeDefault.makeDefault();
                save(processToMakeDefault);
            }
        }
        publisher.publishEvent(new ProcessDeleteEvent(petriNet, EventPhase.POST));
    }

    protected Criteria getProcessRolesCriteria(LoggedUser user) {
        return new Criteria().orOperator(user.getProcessRoles().stream()
                .map(role -> Criteria.where("permissions." + role).exists(true)).toArray(Criteria[]::new));
    }

    @Override
    public void runActions(List<Action> actions, PetriNet petriNet) {
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

    protected void validateAttributes(ImportPetriNetParams importPetriNetParams) throws IllegalArgumentException {
        if (importPetriNetParams.getXmlFile() == null) {
            throw new IllegalArgumentException("No Petriflow source file provided.");
        }
        if (importPetriNetParams.getAuthor() == null) {
            throw new IllegalArgumentException("No author of PetriNet provided.");
        }
        if (importPetriNetParams.getReleaseType() == null) {
            throw new IllegalArgumentException("Version type is null.");
        }
    }

    protected void fillAndValidateAttributes(DeletePetriNetParams deletePetriNetParams) throws IllegalArgumentException {
        if (deletePetriNetParams.getPetriNetId() == null) {
            throw new IllegalArgumentException("No petriNet identifier was provided.");
        }
        if (deletePetriNetParams.getLoggedUser() == null) {
            deletePetriNetParams.setLoggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()));
        }
    }

}
