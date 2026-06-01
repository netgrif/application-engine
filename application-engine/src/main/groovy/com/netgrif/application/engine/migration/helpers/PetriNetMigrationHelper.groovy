package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.Transition
import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorField
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType
import com.netgrif.application.engine.objects.petrinet.domain.events.Event
import com.netgrif.application.engine.objects.petrinet.domain.events.EventType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.springframework.beans.factory.ObjectFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

import java.text.Collator
import java.util.stream.Collectors
/**
 * Helper class for managing Petri Net migration operations in MongoDB.
 * <p>
 * This component provides utilities for migrating and updating Petri Net models, including:
 * <ul>
 *   <li>Updating existing Petri Nets while preserving role references</li>
 *   <li>Managing process roles and permissions</li>
 *   <li>Handling data set migrations</li>
 *   <li>Creating and updating roles in Petri Net models</li>
 *   <li>Bulk operations for efficient database updates</li>
 * </ul>
 * <p>
 * The helper extends {@link AbstractMigrationHelper} to leverage common migration patterns
 * and uses Spring Data MongoDB for database operations.
 *
 * @see AbstractMigrationHelper* @see PetriNet* @see IPetriNetService* @see ProcessRoleRepository
 */
@Slf4j
@Component
class PetriNetMigrationHelper extends AbstractMigrationHelper<PetriNet> {

    /**
     * Service interface for managing Petri Net operations including importing, saving, and retrieving Petri Net models.
     */
    protected final IPetriNetService petriNetService

    /**
     * Repository for persisting and retrieving process roles from the database.
     */
    protected final ProcessRoleRepository processRoleRepository

    /**
     * Provider that supplies {@link Importer} instances for importing Petri Net models from various sources.
     * Uses lazy initialization to create Importer instances on demand.
     */
    protected final ObjectFactory<Importer> importerProvider

    /**
     * Service for managing user-related operations, including retrieving system user for Petri Net imports.
     */
    protected final UserService userService

    /**
     * Constructs a new PetriNetMigrationHelper with the specified dependencies.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     * @param migrationProperties the {@link MigrationProperties} containing migration settings including page size and other configuration
     * @param petriNetService the {@link IPetriNetService} for managing Petri Net operations such as importing, saving, and retrieving Petri Nets
     * @param processRoleRepository the {@link ProcessRoleRepository} for persisting and retrieving process roles from the database
     * @param importerProvider the {@link ObjectFactory} that supplies {@link Importer} instances for importing Petri Net models from various sources
     * @param userService the {@link UserService} for managing user-related operations, including retrieving system user for Petri Net imports
     */
    PetriNetMigrationHelper(MongoTemplate mongoTemplate,
                            MigrationProperties migrationProperties,
                            IPetriNetService petriNetService,
                            ProcessRoleRepository processRoleRepository,
                            ObjectFactory<Importer> importerProvider,
                            UserService userService) {
        super(PetriNet.class, mongoTemplate, migrationProperties)
        this.petriNetService = petriNetService
        this.processRoleRepository = processRoleRepository
        this.importerProvider = importerProvider
        this.userService = userService
    }

    /**
     * Returns the page size for pagination during migration operations.
     *
     * @return the page size configured in {@link MigrationProperties.PetriNetMigrationProperties}
     */
    @Override
    int getPageSize() {
        return migrationProperties.petriNets.pageSize
    }

    /**
     * Prepares bulk operations for updating a Petri Net document in MongoDB.
     *
     * @param document the {@link PetriNet} document to be updated
     * @param update the closure that performs the update operation on the document
     * @param bulkOperations the {@link BulkOperations} to add the replace operation to
     */
    @Override
    void prepareOperations(PetriNet document, Closure update, BulkOperations bulkOperations) {
        log.debug("Updating Petri Net with ID ${document.stringId}")
        update(document)
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(document.getObjectId())), document)
    }

    /**
     * Resolves and returns the string identifier of a Petri Net document.
     * <p>
     * This method is used during migration operations to uniquely identify Petri Net documents
     * when performing bulk operations or logging migration progress.
     *
     * @param document the {@link PetriNet} document whose identifier should be resolved
     * @return the string representation of the Petri Net's unique identifier
     */
    @Override
    String resolveId(PetriNet document) {
        return document.getStringId()
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param resource Resource object with new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, Resource resource, List<Closure<PetriNet>> customUpdates = null) {
        log.debug("Starting updateNetIgnoreRoles for identifier: {} with Resource", identifier)
        PetriNet reimported = petriNetService.importPetriNet(resource.inputStream, VersionType.MAJOR, userService.getSystem().transformToLoggedUser()).getNet()
        updateNetIgnoreRoles(petriNetService.getDefaultVersionByIdentifier(identifier), reimported, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param identifier Identifier of Petri Net model that is being updated
     * @param fileName File name of new version of Petri Net model
     */
    void updateNetIgnoreRoles(String identifier, String fileName, List<Closure<PetriNet>> customUpdates = null) {
        log.debug("Starting updateNetIgnoreRoles for identifier: {} with fileName: {}", identifier, fileName)
        PetriNet currentNet = petriNetService.getDefaultVersionByIdentifier(identifier)
        InputStream inputStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        IOUtils.copy(inputStream, outputStream)
        PetriNet reimported = getImporter().importPetriNet(new ByteArrayInputStream(outputStream.toByteArray()))
                .orElseThrow { new IllegalStateException("Failed to import Petri Net from file: $fileName") }
        updateNetIgnoreRoles(currentNet, reimported, customUpdates)
    }

    /**
     * Updates existing Petri Net model with new values. New process roles are ignored! New roles in existing user type fields will be ignored!
     * @param currentNet Current Petri Net object that will be updated
     * @param reimported New version of Petri Net object, its values will be applied to currentNet
     * @param customUpdates Optional list of custom update closures to be applied after the standard update
     */
    void updateNetIgnoreRoles(PetriNet currentNet, PetriNet reimported, List<Closure<PetriNet>> customUpdates) {
        log.debug("Starting updateNetIgnoreRoles for currentNet: {} and reimported: {}", currentNet?.identifier, reimported?.identifier)
        if (!currentNet) {
            log.warn("Net $reimported.identifier does not exist")
            return
        }
        Map<String, ProcessRole> oldProcessRoles = currentNet.roles
        Map<String, ProcessRole> newProcessRoles = reimported.roles

        reimported = replaceUserFieldRoleReferences(currentNet, reimported)

        ProcessRole defaultRole = processRoleRepository.findAllByName_DefaultValue(ProcessRole.DEFAULT_ROLE, Pageable.ofSize(1)).first()
        ProcessRole anonymousRole = processRoleRepository.findAllByName_DefaultValue(ProcessRole.ANONYMOUS_ROLE, Pageable.ofSize(1)).first()

        currentNet.places = reimported.places
        currentNet.transitions = reimported.transitions
        currentNet.arcs = reimported.arcs
        currentNet.dataSet = reimported.dataSet
        currentNet.transactions = reimported.transactions
        currentNet.importId = reimported.importId
        currentNet.caseEvents = reimported.caseEvents
        currentNet.processEvents = reimported.processEvents
        currentNet.negativeViewRoles = reimported.negativeViewRoles
        currentNet.actorRefs = reimported.actorRefs
        currentNet.functions = reimported.functions

        def newPermissions = [:]
        reimported.permissions.each { id, permissions ->
            log.trace("Processing permission for role id: {}", id)
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
                log.trace("Mapping new role {} to old role {}", newRole.importId, oldRole.stringId)
                newPermissions[oldRole.stringId] = permissions
            }
        }
        currentNet.permissions = newPermissions as Map<String, Map<String, Boolean>>

        currentNet.transitions.each { id, t ->
            log.trace("Processing transition roles for transition: {}", t.importId)
            Map<String, Map<String, Boolean>> oldRoles = new HashMap<>()
            t.roles.each { roleMongoId, permissions ->
                log.trace("Processing transition role with mongoId: {}", roleMongoId)
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
                    log.trace("Mapping transition role {} to old role {}", newRole.importId, oldRole.stringId)
                    oldRoles[oldRole.stringId] = permissions
                }
            }
            t.roles = oldRoles
        }

        resolveDataOrder(currentNet)

        customUpdates && customUpdates.each { Closure<PetriNet> customUpdate ->
            currentNet = customUpdate(currentNet, reimported)
        }

        petriNetService.save(currentNet)
        log.info("Migrated $currentNet.identifier")
    }

    /**
     * Helper method used in updateNetIgnoreRoles method, it sorts PetriNet dataSet alphabetically
     * @param petriNet Instance of Petri Net
     */
    static void resolveDataOrder(PetriNet petriNet, Locale locale = Locale.ROOT) {
        Collator collator = Collator.getInstance(locale)
        List<Field> fields = new LinkedList<>(petriNet.getDataSet().values())
        fields = fields.stream().sorted({ f1, f2 ->
            int comparedTypes = f2.type.name <=> f1.type.name
            if (comparedTypes != 0) return comparedTypes
            return collator.compare((f1.name?.defaultValue ?: f1.stringId), (f2.name?.defaultValue ?: f2.stringId))
        }).collect(Collectors.toList())
        petriNet.dataSet = fields.collectEntries { [(it.getStringId()): (it)] } as LinkedHashMap<String, Field>
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param role ProcessRole that will be updated on transition
     * @param permissions New role permissions on transition
     */
    static void updateTransitionRoles(PetriNet net, String transitionId, ProcessRole role, Map<String, Boolean> permissions) {
        log.debug("Updating transition roles for transitionId: {} in net: {}", transitionId, net?.identifier)
        Transition trans = net.transitions.values().find { it.importId == transitionId }
        if (!trans) {
            log.warn("Transition with importId $transitionId not found in net $net.identifier")
            return
        }
        trans.roles[role.stringId] = permissions
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param net Instance of Petri Net in which role on transition will be updated
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    static void updateTransitionRoles(PetriNet net, String transitionId, String roleImportId, Map<String, Boolean> permissions) {
        ProcessRole role = net.roles.values().find { it.importId == roleImportId }
        if (!role) {
            log.warn("Transition with importId $transitionId not found in net $net.identifier")
            return
        }
        updateTransitionRoles(net, transitionId, role, permissions)
    }

    /**
     * Replaces role permissions on transition with provided map e.g. ["roleId": ["perform": true]]
     * @param transitionId Transition ID of updated transition
     * @param roleImportId ID of a role that will be updated on transition
     * @param permissions New role permissions on transition
     */
    static Closure<PetriNet> updateTransitionRolesClosure(String transitionId, String roleImportId, Map<String, Boolean> permissions) {
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
        log.debug("Starting updateDataSet for identifier: {} with fileName: {}", identifier, fileName)
        PetriNet existing = petriNetService.getDefaultVersionByIdentifier(identifier)
        InputStream inputStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        PetriNet reimported = getImporter().importPetriNet(inputStream)
                .orElseThrow { new IllegalStateException("Failed to import Petri Net from file: $fileName") }

        reimported = replaceUserFieldRoleReferences(existing, reimported)

        existing.dataSet = reimported.dataSet

        if (customUpdate) {
            existing = customUpdate(existing, reimported)
        }

        petriNetService.save(existing)
        log.info("Migrated $identifier")
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    ProcessRole createRoleInNet(String identifier, String id, String title, Map<EventType, Event> events = [:]) {
        log.debug("Creating role in net with identifier: {}, id: {}, title: {}", identifier, id, title)
        return createRoleInNet(identifier, id, new I18nString(title), events)
    }

    /**
     * Create new role in existing Petri Net model.
     * @param identifier Identifier of Petri Net model in which the Process Role will be created
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    ProcessRole createRoleInNet(String identifier, String id, I18nString title, Map<EventType, Event> events = [:]) {
        log.debug("Creating role in net with identifier: {}, id: {}, title: {}", identifier, id, title?.defaultValue)
        PetriNet net = petriNetService.getDefaultVersionByIdentifier(identifier)

        ProcessRole role = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole()
        role.setImportId(id)
        role.setName(title)
        role.setEvents(events)

        role = processRoleRepository.save(role)
        net.addRole(role)
        petriNetService.save(net)

        return role
    }

    /**
     * Updates roles of USER fields in existing Petri Net model, WARNING: new roles referenced in USER fields will be ignored! They need to be migrated manually
     * @param originalNet Current Petri Net object that will be updated
     * @param reimportedNet New version of Petri Net object, its values will be applied to currentNet
     * @return the updated reimported Petri Net with replaced role references
     */
    private static PetriNet replaceUserFieldRoleReferences(PetriNet originalNet, PetriNet reimportedNet) {
        Map<String, ProcessRole> originalNetRoles = [:] // importId: processRole
        originalNet.roles.forEach { name, role ->
            originalNetRoles.put(role.importId, role)
        }

        reimportedNet.dataSet.entrySet().stream().filter {
            it.value.type == FieldType.ACTOR

        }.forEach { entry ->
            log.trace("Processing user field role references for field: {}", entry.key)
            ActorField field = (reimportedNet.dataSet[entry.key] as ActorField)
            field.roles = field.roles.collect { roleId ->
                Optional<ProcessRole> roleOpt = Optional.ofNullable(reimportedNet.roles[roleId])
                if (roleOpt.isPresent()) {
                    ProcessRole oldRole = originalNetRoles[roleOpt.get().importId]

                    if (!oldRole) {
                        log.warn("Process role in process ${originalNet.identifier} ${originalNet.stringId} with import id ${roleOpt.get().importId} not found!")
                        return null

                    } else {
                        log.trace("Mapped user field role {} to {}", roleOpt.get().importId, oldRole.stringId)
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
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    ProcessRole createGlobalRole(String id, String title, Map<EventType, Event> events = [:]) {
        log.debug("Creating global role with id: {}, title: {}", id, title)
        return createGlobalRole(id, new I18nString(title), events)
    }

    /**
     * Creates new global role
     * @param id ID of the new Process Role
     * @param title Title of the new Process Role
     */
    ProcessRole createGlobalRole(String id, I18nString title, Map<EventType, Event> events = [:]) {
        log.debug("Creating global role with id: {}, title: {}", id, title?.defaultValue)
        ProcessRole role = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole()

        if (!id.startsWith("global_")) {
            role.setImportId("global_" + id)
        } else {
            role.setImportId(id)
        }
        role.setName(title)
        role.setEvents(events)
        role.setGlobal(true)

        role = processRoleRepository.save(role)

        return role
    }

    /**
     * Replaces events in roles from existing with events from roles from reimported
     * @param existing the existing {@link PetriNet} whose role events will be updated
     * @param reimported the reimported {@link PetriNet} containing new role events
     * @return the updated existing Petri Net
     */
    PetriNet updateRoleEvents(PetriNet existing, PetriNet reimported) {
        log.debug("Starting updateRoleEvents for existing net: {} and reimported net: {}", existing?.identifier, reimported?.identifier)
        List<ProcessRole> newRoles = reimported.roles.values() as List
        List<ProcessRole> oldRoles = existing.roles.values() as List

        newRoles.each { newRole ->
            log.trace("Processing role events for role: {}", newRole.importId)
            ProcessRole role = oldRoles.find { it.importId == newRole.importId }
            if (!role) {
                log.warn("No existing role found for importId $newRole.importId, skipping event update")
                return
            }
            role.events = newRole.events
            processRoleRepository.save(role)
        }

        return existing
    }

    /**
     * Sets petriNet object in case instance
     * @param useCase Instance of Case
     * @param net Instance of Petri Net, it needs to match processIdentifier of useCase
     */
    static void setPetriNet(Case useCase, PetriNet net) {
        log.debug("Setting PetriNet for case: {} with net: {}", useCase?.stringId, net?.identifier)
        PetriNet model = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet(net as com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet)
        model.initializeTokens(useCase.getActivePlaces())
        model.initializeArcs(useCase.getDataSet())
        useCase.setPetriNet(model)
    }

    /**
     * Provides an {@link com.netgrif.application.engine.importer.service.Importer} instance
     * @return a new {@link Importer} instance from the provider
     * */
    Importer getImporter() {
        return importerProvider.getObject()
    }

    /**
     * Method that collects all dataRef components of given PetriNet. Should be used in updateCases method, when a new dataRef component is added into PetriNet.
     * @param net Instance of PetriNet
     */
    static Map<String, Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component>> createDataRefComponentsMap(PetriNet net) {
        log.debug("Creating dataRef components map for net: {}", net?.identifier)
        Map<String, Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component>> componentsMap = [:]
        net.transitions.each { transition ->
            String transId = transition.key
            transition.value.dataSet.each { dataField ->
                String fieldId = dataField.key
                if (dataField.value.component) {
                    log.trace("Adding dataRef component for field: {} in transition: {}", fieldId, transId)
                    if (!componentsMap[fieldId]) {
                        componentsMap.put(fieldId, [(transId) : dataField.value.component])
                    } else {
                        Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component> existingMap = componentsMap[fieldId]
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
    static Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component> createComponentsMap(PetriNet net) {
        log.debug("Creating components map for net: {}", net?.identifier)
        Map<String, com.netgrif.application.engine.objects.petrinet.domain.Component> componentsMap = [:]
        net.dataSet.each { dataField ->
            if (dataField.value.component) {
                log.trace("Adding component for field: {}", dataField.key)
                componentsMap.put(dataField.key, dataField.value.component)
            }
        }
        return componentsMap
    }
}
