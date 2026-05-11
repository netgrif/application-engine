package com.netgrif.application.engine.migration

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.*
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.Transition
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.events.Event
import com.netgrif.application.engine.petrinet.domain.events.EventType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.PetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.*
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import javax.inject.Provider
import java.text.Collator
import java.time.LocalDateTime
import java.util.stream.Collectors

@Slf4j
@Component
class MigrationHelper {

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private PetriNetService service

    @Autowired
    private Provider<Importer> importerProvider

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private ITaskService taskService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private IElasticCaseMappingService caseMappingService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private IElasticTaskMappingService elasticTaskMappingService

    @Autowired
    private IUserService userService

    @Autowired
    private IElasticIndexService elasticIndexService

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private IPetriNetService petriNetService

    private Importer getImporter() {
        return importerProvider.get()
    }

    /**
     * Updates all cases filtered by filter Predicate. Update closure is called on each filtered case.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param filter Instance of Predicate, to filter which cases should be updated
     */
    void updateCases(Closure update, Predicate filter) {
        log.info("Updating cases with filter ${filter.toString()} and update ${update.toString()}")
        iterateCases(update, { Page<Case> cases -> caseRepository.saveAll(cases) }, filter)
    }

    /**
     * Iterates all cases filtered by filter Predicate. Update closure is called on each filtered case. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter (changes made to Case will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which cases should be iterated
     */
    void iterateCases(Closure update, Closure pageProcessed = {}, long sleepFor = 0, Predicate filter) {
        long caseCount = caseRepository.count(filter)
        long numOfPages = ((caseCount / 100.0) + 1) as long
        log.info("Processing cases with filter ${filter.toString()}: $numOfPages pages")
        numOfPages.times { page ->
            log.info("Page $page / $numOfPages")

            Page<Case> cases = caseRepository.findAll(filter, PageRequest.of(page, 100))

            cases.each {
                log.debug("Processing case with id ${it.stringId}")
                log.trace("Processing case ${it.toString()}")
                update(it)
            }
            pageProcessed(cases)
            if (sleepFor != 0) {
                log.debug("Pausing migration for ${sleepFor} milliseconds")
                sleep(sleepFor)
            }
        }
    }

    /**
     * Updates all cases of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Case matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which cases should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateCasesCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        long caseCount = caseRepository.count(QCase.case$.processIdentifier.eq(processIdentifier))
        long numOfPages = ((caseCount / pageSize) + 1) as long
        log.info("Migrating process $processIdentifier")
        log.info("Page size: $pageSize")
        log.info("Processing cases: $numOfPages pages")
        ObjectId lastId = null
        if (caseCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.addCriteria(Criteria.where("processIdentifier").is(processIdentifier))
                    query.limit(pageSize as Integer)

                    List<Case> cases = mongoTemplate.find(query, Case.class)
                    cases.each { update(it) }
                    cases = caseRepository.saveAll(cases)

                    lastId = cases.get(cases.size() - 1).get_id()
                } catch (Exception e) {
                    log.error("Failed to iterate page " + (p + 1), e.getMessage())
                    break
                }
            }
        }
    }

    /**
     * Update all cases.
     * @param update Instance of Closure, which should contain code that will be executed for every Case
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllCasesCursor(Closure update, double pageSize = 100.0) {
        long caseCount = caseRepository.count()
        long numOfPages = ((caseCount / pageSize) + 1) as long
        log.info("Page size: $pageSize")
        log.info("Processing cases: $numOfPages pages")
        ObjectId lastId = null
        if (caseCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.limit(pageSize as Integer)

                    List<Case> cases = mongoTemplate.find(query, Case.class)
                    cases.each { update(it) }
                    cases = caseRepository.saveAll(cases)

                    lastId = cases.get(cases.size() - 1).get_id()
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Failed to iterate page " + (p + 1))
                    break
                }
            }
        }
    }

    /**
     * Updates all tasks filtered by filter Predicate. Update closure is called on each filtered task.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param filter Instance of Predicate, to filter which tasks should be updated
     */
    void updateTasks(Closure update, Predicate filter) {
        log.info("Updating tasks with filter ${filter.toString()} and update ${update.toString()}")
        iterateTasks(update, { Page<Task> tasks -> taskRepository.saveAll(tasks) }, filter)
    }

    /**
     * Iterates all tasks filtered by filter Predicate. Update closure is called on each filtered task. PageProcessed closure is called after each page iteration.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter (changes made to Task will not be saved automatically, for that use updateCases method)
     * @param sleepFor Optional attribute to set sleep time (in milliseconds) to sleep for after each iterated page. Default 0ms
     * @param filter Instance of Predicate, to filter which tasks should be iterated
     */
    void iterateTasks(Closure update, Closure pageProcessed = {}, long sleepFor = 0, Predicate filter) {
        long taskCount = taskRepository.count(filter)
        long numOfPages = ((taskCount / 100.0) + 1) as long
        log.info("Processing tasks with filter ${filter.toString()}: $numOfPages pages")
        numOfPages.times { page ->
            log.info("Page $page / $numOfPages")

            Page<Task> tasks = taskRepository.findAll(filter, PageRequest.of(page, 100))

            tasks.each { update(it) }
            pageProcessed(tasks)
            if (sleepFor != 0) {
                sleep(sleepFor)
            }
        }
    }

    /**
     * Updates all tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateTasksCursor(Closure update, String processIdentifier, double pageSize = 100.0) {
        String processId = petriNetService.getNewestVersionByIdentifier(processIdentifier).stringId
        long taskCount = taskRepository.count(QTask.task.processId.eq(processId))
        long numOfPages = ((taskCount / pageSize) + 1) as long
        log.info("Migrating process $processIdentifier")
        log.info("Page size: $pageSize")
        log.info("Processing tasks: $numOfPages pages")
        ObjectId lastId = null
        if (taskCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.addCriteria(Criteria.where("processId").is(processId))
                    query.limit(pageSize as Integer)

                    List<Task> tasks = mongoTemplate.find(query, Task.class)
                    tasks.each { update(it) }
                    tasks = taskRepository.saveAll(tasks)

                    lastId = tasks.get(tasks.size() - 1).objectId
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Failed to iterate page " + (p + 1))
                    break
                }
            }
        }
    }

    /**
     * Updates specific tasks of a given process.
     * @param update Instance of Closure, which should contain code that will be executed for every Task matched by filter
     * @param processIdentifier identifier of PetriNet, to filter which tasks should be updated
     * @param transitionIds List of transition IDs to limit filter to specific transitions of given processIdentifier
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateSpecificTasksCursor(Closure update, String processIdentifier, List<String> transitionIds, double pageSize = 100.0) {
        String processId = petriNetService.getNewestVersionByIdentifier(processIdentifier).stringId
        long taskCount = taskRepository.count(QTask.task.processId.eq(processId) & QTask.task.transitionId.in(transitionIds))
        long numOfPages = ((taskCount / pageSize) + 1) as long
        log.info("Migrating process $processIdentifier transitions ${transitionIds.toString()}")
        log.info("Page size: $pageSize")
        log.info("Processing tasks: $numOfPages pages")
        ObjectId lastId = null
        if (taskCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.addCriteria(Criteria.where("processId").is(processId))
                    query.addCriteria(Criteria.where("transitionId").in(transitionIds))
                    query.limit(pageSize as Integer)

                    List<Task> tasks = mongoTemplate.find(query, Task.class)
                    tasks.each { update(it) }
                    tasks = taskRepository.saveAll(tasks)

                    lastId = tasks.get(tasks.size() - 1).objectId
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Failed to iterate page " + (p + 1))
                    break
                }
            }
        }
    }

    /**
     * Update all tasks.
     * @param update Instance of Closure, which should contain code that will be executed for every Task
     * @param pageSize Optional attribute to set page size. Default page size 100.0
     */
    void updateAllTasksCursor(Closure update, double pageSize = 100.0) {
        long taskCount = taskRepository.count()
        long numOfPages = ((taskCount / pageSize) + 1) as long
        log.info("Page size: $pageSize")
        log.info("Processing tasks: $numOfPages pages")
        ObjectId lastId = null
        if (taskCount > 0) {
            for (int p = 0; p < numOfPages; p++) {
                try {
                    log.info("Page " + (p + 1) + " / $numOfPages")

                    Query query = new Query()
                    if (lastId == null) {
                        query.skip(0)
                    } else {
                        query.addCriteria(Criteria.where("_id").gt(lastId))
                    }
                    query.limit(pageSize as Integer)

                    List<Task> tasks = mongoTemplate.find(query, Task.class)
                    tasks.each { update(it) }
                    tasks = taskRepository.saveAll(tasks)

                    lastId = tasks.get(tasks.size() - 1).objectId
                } catch (ArrayIndexOutOfBoundsException e) {
                    log.error("Failed to iterate page " + (p + 1))
                    break
                }
            }
        }
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param resource Resource object with new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, Resource resource, List<Closure<PetriNet>> customUpdates = null) {
        PetriNet reimported = service.importPetriNet(resource.inputStream, VersionType.MAJOR, userService.system.transformToLoggedUser()).getNet()
        updateNetIgnoreRoles(service.getNewestVersionByIdentifier(identifier), reimported, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, String fileName, List<Closure<PetriNet>> customUpdates = null) {
        PetriNet currentNet = service.getNewestVersionByIdentifier(identifier)
        InputStream inputStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        IOUtils.copy(inputStream, outputStream)
        PetriNet reimported = getImporter().importPetriNet(new ByteArrayInputStream(outputStream.toByteArray())).get()
        updateNetIgnoreRoles(currentNet, reimported, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param currentNet Current Petri Net object that will be updated
     * @param reimported New version of Petri Net object, its values will be applied to currentNet
     */
    void updateNetIgnoreRoles(PetriNet currentNet, PetriNet reimported, List<Closure<PetriNet>> customUpdates) {
        if (!currentNet) {
            log.warn("Net $reimported.identifier does not exist")
            return
        }
        Map<String, ProcessRole> oldProcessRoles = currentNet.roles
        Map<String, ProcessRole> newProcessRoles = reimported.roles

        reimported = replaceUserFieldRoleReferences(currentNet, reimported)

        ProcessRole defaultRole = roleRepository.findAllByName_DefaultValue(ProcessRole.DEFAULT_ROLE).first()
        ProcessRole anonymousRole = roleRepository.findAllByName_DefaultValue(ProcessRole.ANONYMOUS_ROLE).first()

        currentNet.places = reimported.places
        currentNet.transitions = reimported.transitions
        currentNet.arcs = reimported.arcs
        currentNet.dataSet = reimported.clone().dataSet
        currentNet.transactions = reimported.transactions
        currentNet.importId = reimported.importId
        currentNet.caseEvents = reimported.caseEvents
        currentNet.processEvents = reimported.processEvents
        currentNet.negativeViewRoles = reimported.negativeViewRoles
        currentNet.userRefs = reimported.userRefs
        currentNet.functions = reimported.functions

        def newPermissions = [:]
        reimported.permissions.each { id, permissions ->
            def newRole = newProcessRoles[id]

            if (!newRole && (defaultRole.stringId == id || anonymousRole.stringId == id)) {
                log.info("Default role $id on process $currentNet.identifier detected, skipping")
                newPermissions[id] = permissions

            } else {
                def oldRole = oldProcessRoles.values().find {
                    it.importId == newRole.importId
                }

                if (!oldRole) {
                    log.warn("Old role does not exist for role $newRole.importId")
                    return
                }
                newPermissions[oldRole.stringId] = permissions
            }
        }
        currentNet.permissions = newPermissions as Map<String, Map<String, Boolean>>

        currentNet.transitions.each { id, t ->
            Map<String, Map<String, Boolean>> oldRoles = new HashMap<>()
            t.roles.each { roleMongoId, permissions ->
                def newRole = newProcessRoles[roleMongoId]

                if (!newRole && (defaultRole.stringId == roleMongoId || anonymousRole.stringId == roleMongoId)) {
                    log.info("Default role $roleMongoId on transition ${t.importId} detected, skipping")
                    oldRoles[roleMongoId] = permissions

                } else {
                    def oldRole = oldProcessRoles.values().find {
                        it.importId == newRole.importId
                    }

                    if (!oldRole) {
                        log.warn("Old role does not exist for role $newRole.importId")
                        return
                    }
                    oldRoles[oldRole.stringId] = permissions
                }
            }
            t.roles = oldRoles
        }

        resolveDataOrder(currentNet)

        customUpdates && customUpdates.each { Closure<PetriNet> customUpdate ->
            currentNet = customUpdate(currentNet, reimported)
        }

        service.save(currentNet)
        log.info("Migrated $currentNet.identifier")
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param role ProcessRole that will be updated on transition
     * @param permissions New role permissions on transition
     */
    void updateTransitionRoles(PetriNet net, String transitionId, ProcessRole role, Map<String, Boolean> permissions) {
        Transition trans = net.transitions.values().find { it.importId == transitionId }
        trans.roles[role.stringId] = permissions
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    void updateTransitionRoles(PetriNet net, String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        ProcessRole role = net.roles.values().find { it.importId == roleImportId }
        updateTransitionRoles(net, transitionId, role, permissions)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    Closure<PetriNet> updateTransitionRolesClosure(String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        return { PetriNet petriNet, PetriNet reimported ->
            updateTransitionRoles(petriNet, transitionId, roleImportId, permissions)
            return petriNet
        }
    }

    /**
     * Updates data set of existing Petri Net model with new values.
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateDataSet(String identifier, String fileName, Closure<PetriNet> customUpdate = null) {
        PetriNet existing = service.getNewestVersionByIdentifier(identifier)
        PetriNet reimported = getImporter().importPetriNet(new File("src/main/resources/petriNets/" + fileName)).get()

        reimported = replaceUserFieldRoleReferences(existing, reimported)

        existing.dataSet = reimported.dataSet

        if (customUpdate) {
            existing = customUpdate(existing, reimported)
        }

        service.save(existing)
        log.info("Migrated $identifier")
    }

    /**
     * Updates roles of USER fields in existing Petri Net model, WARNING: new roles referenced in USER fields will be ignored! They need to be migrated manually
     * @param originalNet Current Petri Net object that will be updated
     * @param reimportedNet New version of Petri Net object, its values will be applied to currentNet
     */
    private PetriNet replaceUserFieldRoleReferences(PetriNet originalNet, PetriNet reimportedNet) {
        Map<String, ProcessRole> originalNetRoles = [:] // importId: processRole
        originalNet.roles.forEach { name, role ->
            originalNetRoles.put(role.importId, role)
        }

        reimportedNet.dataSet.entrySet().stream().filter {
            it.value.type == FieldType.USER

        }.forEach { entry ->
            UserField field = (reimportedNet.dataSet[entry.key] as UserField)
            field.roles = field.roles.collect { roleId ->
                Optional<ProcessRole> roleOpt = Optional.ofNullable(reimportedNet.roles[roleId])
                if (roleOpt.isPresent()) {
                    ProcessRole oldRole = originalNetRoles[roleOpt.get().importId]

                    if (!oldRole) {
                        log.warn("Process role in process ${originalNet.identifier} ${originalNet.stringId} with import id ${roleOpt.get().importId} not found!")
                        return null

                    } else {
                        return oldRole.stringId
                    }

                } else {
                    log.warn("Role not found! ${roleId}")
                    return null
                }
            }.stream().filter { Objects.nonNull(it) }.collect()

        }

        return reimportedNet
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, String title, Map<EventType, Event> events = [:]) {
        return createRoleInNet(identifier, id, new I18nString(title), events)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createRoleInNet(String identifier, String id, I18nString title, Map<EventType, Event> events = [:]) {
        PetriNet net = service.getNewestVersionByIdentifier(identifier)

        ProcessRole role = new ProcessRole()
        role.setImportId(id)
        role.setName(title)
        role.setEvents(events)

        role = roleRepository.save(role)
        net.addRole(role)
        netRepository.save(net)

        return role
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, String title, Map<EventType, Event> events = [:]) {
        return createGlobalRole(id, new I18nString(title), events)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    def createGlobalRole(String id, I18nString title, Map<EventType, Event> events = [:]) {
        ProcessRole role = new ProcessRole()

        if (!id.startsWith("global_")) {
            role.setImportId("global_" + id)
        } else {
            role.setImportId(id)
        }
        role.setName(title)
        role.setEvents(events)
        role.setGlobal(true)

        role = roleRepository.save(role)

        return role
    }

    /**
     * Replaces events in roles from existing with events from roles from reimported
     */
    Closure<PetriNet> updateRoleEvents = { PetriNet existing, PetriNet reimported ->
        List<ProcessRole> newRoles = reimported.roles.values() as List
        List<ProcessRole> oldRoles = existing.roles.values() as List

        newRoles.each { newRole ->
            ProcessRole role = oldRoles.find { it.importId == newRole.importId }
            role.events = newRole.events
            roleRepository.save(role)
        }

        return existing
    }

    /**
     * Reloads tasks of provided case via TaskService,
     * handles useCase.petriNet internally
     * @param useCase Instance of Case for which tasks will be reloaded
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void reloadTasks(Case useCase, PetriNet net) {
        setPetriNet(useCase, net)
        taskService.reloadTasks(useCase)
    }

    /**
     * Indexes provided case in elasticsearch
     * handles useCase.petriNet internally
     * @param useCase Instance of Case that will be indexed into elasticsearch index
     */
    void elasticIndex(Case useCase) {
        try {
            setPetriNet(useCase, service.getNewestVersionByIdentifier(useCase.processIdentifier))
            assert useCase.petriNet
            elasticCaseService.indexNow(caseMappingService.transform(useCase))
        } catch (Exception ex) {
            if (useCase.lastModified == null) {
                log.error("Creating new lastModified date for $useCase.stringId")
                useCase.lastModified = LocalDateTime.now()
                elasticCaseService.indexNow(caseMappingService.transform(useCase))
            } else {
                log.error("Failed to index $useCase.stringId", ex)
            }
        }
    }

    /**
     * Indexes provided task in elasticsearch
     * @param task Instance of Task that will be indexed into elasticsearch index
     */
    void elasticTaskIndex(Task task) {
        try {
            elasticTaskService.indexNow(elasticTaskMappingService.transform(task))
        } catch (Exception e) {
            log.error("Failed to index $task.stringId", e)
        }
    }

    /**
     * Adds role with permissions to existing tasks of net
     * @param role ProcessRole that will be added to transitions
     * @param net Instance of Petri Net of updated transitions
     * @param transitionIds List of transition IDs the role will be added to
     * @param permissions Map of permissions for the role
     */
    void addRoleToExistingTasks(ProcessRole role, PetriNet net, List<String> transitionIds, Map<String, Boolean> permissions) {
        updateTasks({ Task task ->
            log.info("Add role '${role.getName()}' with roleId=${role.getImportId()} to transitionId=${task.getTransitionId()} in task ${task.stringId}")
            task.addRole(role.getStringId(), permissions)
        }, QTask.task.transitionId.in(transitionIds) & QTask.task.processId.eq(net.getStringId()))
    }

    /**
     * Sets petriNet object in case instance
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void setPetriNet(Case useCase, PetriNet net) {
        PetriNet model = net.clone()
        model.initializeTokens(useCase.getActivePlaces())
        model.initializeArcs(useCase.getDataSet())
        useCase.setPetriNet(model)
    }

    /**
     * Delete given data fields from useCase
     * @param useCase Instance of Case
     * @param toDelete List of field IDs that will be deleted from useCase
     */
    void deleteDataFields(Case useCase, List<String> toDelete) {
        toDelete.each { dataFieldID ->
            useCase.dataSet.remove(dataFieldID)
        }
    }

    /**
     * Changes value of given data fields from number to text
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromNumberToText(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && (useCase.dataSet[dataFieldID].value != null || useCase.dataSet[dataFieldID].value != "")) {
                double value = useCase.dataSet[dataFieldID].value as double
                useCase.dataSet[dataFieldID].value = value as String
            }
        }
    }

    /**
     * Changes value of given data fields from text to number
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromTextToNumber(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && useCase.dataSet[dataFieldID].value != "") {
                try {
                    useCase.dataSet[dataFieldID].value = useCase.dataSet[dataFieldID].value as double
                } catch (Exception e) {
                    useCase.dataSet[dataFieldID].value = null
                    log.error("[${useCase.stringId}] could not convert value ${useCase.dataSet[dataFieldID].value} in field ${dataFieldID}", e)
                }
            }
        }
    }

    /**
     * Adds new data fields with their init value into useCase
     * @param useCase Instance of Case
     * @param toAdd Map<field id, init value of field>
     */
    void addTextDataFields(Case useCase, Map<String, String> toAdd) {
        toAdd.each { dataFieldID, value ->
            useCase.dataSet[dataFieldID] = new DataField(value)
        }
    }

    /**
     * Changes value of given data fields from enumeration to multichoice
     * @param useCase Instance of Case
     * @param toChange List of field IDs for value change
     */
    void changeDataFieldsValueFromEnumerationToMultichoice(Case useCase, List<String> toChange) {
        toChange.each { dataFieldID ->
            if (useCase.dataSet[dataFieldID].value && useCase.dataSet[dataFieldID].value != null) {
                def value
                if (useCase.dataSet[dataFieldID].value instanceof I18nString) {
                    value = useCase.dataSet[dataFieldID].value as I18nString
                } else {
                    value = new I18nString(useCase.dataSet[dataFieldID].value as String)
                }

                def newSet = new HashSet<I18nString>()
                newSet.add(value)
                useCase.dataSet[dataFieldID].value = newSet
            }
        }
    }

    /**
     * Adds new choices into enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data data field>
     */
    void addChoices(Case useCase, Map<String, List<String>> toAdd) {
        toAdd.each { dataFieldID, newChoices ->
            if (useCase.dataSet[dataFieldID].choices == null) {
                useCase.dataSet[dataFieldID].setChoices(new HashSet<I18nString>())
            }

            newChoices.each {
                useCase.dataSet[dataFieldID].choices.add(new I18nString(it))
            }
        }
    }

    /**
     * Removes choices from enumeration or multichoice field
     * @param useCase Instance of Case
     * @param toAdd Map<field id, list of choices to add into data field>
     */
    void removeChoices(Case useCase, Map<String, List<String>> toRemove) {
        toRemove.each { dataFieldID, choicesToRemove ->
            if (useCase.dataSet[dataFieldID].value != null) {
                (useCase.dataSet[dataFieldID].value as Set).removeAll(choicesToRemove)
            }

            if (useCase.dataSet[dataFieldID].choices != null) {
                useCase.dataSet[dataFieldID].choices.removeAll(choicesToRemove)
            }
        }
    }

    /**
     * Changes value from FileFieldValue to FileListFieldValue
     * @param useCase Instance of Case
     * @param fieldId Field ID for value change
     */
    private void changeFileFieldToFileList(Case useCase, String fieldId) {
        FileListFieldValue fileListFieldValue = new FileListFieldValue()
        fileListFieldValue.namesPaths.add(useCase.dataSet[fieldId].value as FileFieldValue)
        useCase.dataSet[fieldId].value = fileListFieldValue
    }

    /**
     * Helper method used in updateNetIgnoreRoles method, it sorts PetriNet dataSet alphabetically
     * @param petriNet Instance of Petri Net
     */
    void resolveDataOrder(PetriNet petriNet) {
        Collator skCollator = Collator.getInstance(new Locale("sk", "SK"))
        List<Field> fields = new LinkedList<>(petriNet.getDataSet().values())
        fields = fields.stream().sorted({ f1, f2 ->
            int comparedTypes = f2.type.name <=> f1.type.name
            if (comparedTypes != 0) return comparedTypes
            return skCollator.compare((f1.name?.defaultValue ?: f1.stringId), (f2.name?.defaultValue ?: f2.stringId))
        }).collect(Collectors.toList())
        petriNet.dataSet = fields.collectEntries { [(it.getStringId()): (it)] } as Map<String, Field>
    }

    /**
     * Update dataField and dataRef components of given case
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCaseComponents(Case useCase, PetriNet net) {
        Map<String, com.netgrif.application.engine.petrinet.domain.Component> components = createComponentsMap(net)
        Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> dataRefComponents = createDataRefComponentsMap(net)

        useCase.dataSet.each {dataField ->
            if (components[dataField.key]) {
                useCase.dataSet[dataField.key].component = components[dataField.key]
            }
            if (dataRefComponents[dataField.key]) {
                useCase.dataSet[dataField.key].dataRefComponents = dataRefComponents[dataField.key]
            }
        }
    }

    /**
     * Method that collects all dataRef components of given PetriNet. Should be used in updateCases method, when a new dataRef component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> createDataRefComponentsMap(PetriNet net) {
        Map<String, Map<String, com.netgrif.application.engine.petrinet.domain.Component>> componentsMap = [:]
        net.transitions.each {transition ->
            String transId = transition.key
            transition.value.dataSet.each {dataField ->
                String fieldId = dataField.key
                if (dataField.value.component) {
                    if (!componentsMap[fieldId]) {
                        componentsMap.put(fieldId, [(transId) : dataField.value.component])
                    } else {
                        Map<String, com.netgrif.application.engine.petrinet.domain.Component> existingMap = componentsMap[fieldId]
                        existingMap.put(transId, dataField.value.component)
                        componentsMap.put(fieldId, existingMap)
                    }
                }
            }
        }
        return componentsMap
    }

    /**
     * Method that collects all dataField components of given PetriNet. Should be used in updateCases method, when a new dataField component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    Map<String, com.netgrif.application.engine.petrinet.domain.Component> createComponentsMap(PetriNet net) {
        Map<String, com.netgrif.application.engine.petrinet.domain.Component> componentsMap = [:]
        net.dataSet.each {dataField ->
            if (dataField.value.component) {
                componentsMap.put(dataField.key, dataField.value.component)
            }
        }
        return componentsMap
    }

    /**
     * Updates case permissions from PetriNet
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateCasePermissionsFromNet(Case useCase, PetriNet net, boolean updateTasks = false) {
        useCase.permissions = net.getPermissions().entrySet().stream()
                .filter(role -> role.getValue().containsKey("delete") || role.getValue().containsKey("view"))
                .map(role -> {
                    Map<String, Boolean> permissionMap = new HashMap<>()
                    if (role.getValue().containsKey("delete"))
                        permissionMap.put("delete", role.getValue().get("delete"))
                    if (role.getValue().containsKey("view")) {
                        permissionMap.put("view", role.getValue().get("view"))
                    }
                    return new AbstractMap.SimpleEntry<>(role.getKey(), permissionMap)
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))
        useCase.resolveViewRoles()
        useCase.setEnabledRoles(net.getRoles().keySet())
        if (updateTasks) {
            useCase.tasks.each { taskPair ->
                updateTaskPermissions(useCase, taskPair, net)
            }
        }
    }

    /**
     * Updates permissions on existing tasks filtered by relevantTransitionIds
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     * @param relevantTransitionIds List of transition IDs for permissions update
     */
    void updateTasksPermissions(Case useCase, PetriNet net, List<String> relevantTransitionIds) {
        useCase.tasks.findAll { it.transition in relevantTransitionIds }.each { taskPair ->
            updateTaskPermissions(useCase, taskPair, net)
        }
    }

    /**
     * Updates permissions on existing task
     * @param useCase Instance of Case
     * @param taskPair TaskPair object of updated Task
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void updateTaskPermissions(Case useCase, TaskPair taskPair, PetriNet net) {
        try {
            Transition newTransition = net.getTransition(taskPair.transition)
            Task oldTask = taskService.findOne(taskPair.task)
            oldTask.setProcessId(net.stringId)
            oldTask.getRoles().clear()
            oldTask.setRoles(newTransition.roles)
            oldTask.setNegativeViewRoles(newTransition.negativeViewRoles)
            oldTask.resolveViewRoles()
            taskService.save(oldTask)
        } catch (Exception e) {
            log.error("Failed to update task permissions $useCase.stringId $taskPair.transition", e)
        }
    }

    /**
     * Changes PetriNet reference in useCase
     * @param useCase Instance of Case
     * @param newNet Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    void migratePetriNet(Case useCase, PetriNet newNet) {
        useCase.setPetriNetObjectId(newNet.objectId)
    }
}