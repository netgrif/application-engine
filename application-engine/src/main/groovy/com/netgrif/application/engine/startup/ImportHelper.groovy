package com.netgrif.application.engine.startup

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.domain.Authority

import com.netgrif.application.engine.objects.auth.domain.LoggedUser
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome

//import com.netgrif.application.engine.workflow.service.interfaces.IFilterService

import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class ImportHelper {

    public static final String PATCH = "patch"

    public static final String FIELD_BOOLEAN = "boolean"
    public static final String FIELD_ENUMERATION = "enumeration"
    public static final String FIELD_TEXT = "text"
    public static final String FIELD_NUMBER = "number"
    public static final String FIELD_DATE = "date"

    private static final Logger log = LoggerFactory.getLogger(ImportHelper.class.name)

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private UserService userService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ResourceLoader resourceLoader

//    @Autowired
//    private IFilterService filterService

    @Autowired(required = false)
    private SuperCreatorRunner superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private GroupService groupService

    @Autowired
    private ProcessRoleService processRoleService

    private final ClassLoader loader = ImportHelper.getClassLoader()


    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Authority> createAuthorities(Map<String, String> authorities) {
        HashMap<String, Authority> authoritities = new HashMap<>()
        authorities.each { authority ->
            authoritities.put(authority.key, authorityService.getOrCreate(authority.value))
        }

        log.info("Creating ${authoritities.size()} authorities")
        return authoritities
    }

    Authority createAuthority(String name) {
        log.info("Creating authority $name")
        return authorityService.getOrCreate(name)
    }

    Optional<PetriNet> createNet(String fileName, String release, LoggedUser author = ActorTransformer.toLoggedUser(userService.getSystem())) {
        return createNet(fileName, VersionType.valueOf(release.trim().toUpperCase()), author)
    }

    Optional<PetriNet> createNet(String fileName, VersionType release = VersionType.MAJOR, LoggedUser author = ActorTransformer.toLoggedUser(userService.getSystem())) {
        InputStream netStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        def outcome = petriNetService.importPetriNet(new ImportPetriNetParams(netStream, release, author))
        PetriNet petriNet = outcome.getNet()
        if (petriNet == null) {
                log.warn("Import of [$fileName] produced no PetriNet object")
                return Optional.empty()
            }
        log.info("Imported '${petriNet.title?.defaultValue}' ['${petriNet.identifier}', ${petriNet.stringId}]")
        return Optional.of(petriNet)
    }

    Optional<PetriNet> upsertNet(String filename, String identifier, VersionType release = VersionType.MAJOR, LoggedUser author = ActorTransformer.toLoggedUser(userService.getSystem())) {
        PetriNet petriNet = petriNetService.getDefaultVersionByIdentifier(identifier)
        if (!petriNet) {
            return createNet(filename, release, author)
        }
        return Optional.of(petriNet)
    }

//    ProcessRole createUserProcessRole(PetriNet net, String name) {
//        ProcessRole role = processRoleRepository.save(new ProcessRole(roleId:
//                net.roles.values().find { it -> it.name.defaultValue == name }.stringId, netId: net.getStringId()))
//        log.info("Created user process role $name")
//        return role
//    }
//
//    Map<String, ProcessRole> createUserProcessRoles(Map<String, String> roles, PetriNet net) {
//        HashMap<String, ProcessRole> userRoles = new HashMap<>()
//        roles.each { it ->
//            userRoles.put(it.key, createUserProcessRole(net, it.value))
//        }
//
//        log.info("Created ${userRoles.size()} process roles")
//        return userRoles
//    }


    ProcessRole getProcessRoleByImportId(PetriNet net, String roleId) {
        ProcessRole role = net.roles.values().find { it -> it.importId == roleId }
        return role
    }

    Map<String, ProcessRole> getProcessRolesByImportId(PetriNet net, Map<String, String> importId) {
        HashMap<String, ProcessRole> roles = new HashMap<>()
        importId.each { it ->
            roles.put(it.getKey(), getProcessRoleByImportId(net, it.getValue()))
        }
        return roles
    }

    Map<String, ProcessRole> getProcessRoles(PetriNet net) {
        List<ProcessRole> roles = processRoleService.findAllByNetStringId(net.stringId)
        Map<String, ProcessRole> map = [:]
        net.roles.values().each { netRole ->
            map[netRole.name.getDefaultValue()] = roles.find { it.stringId == netRole.stringId }
        }
        return map
    }

    AbstractUser createUser(User user, Authority[] authorities, ProcessRole[] roles) {
        authorities.each { user.addAuthority(it) }
        roles.each { user.addProcessRole(it) }
        user.state = UserState.ACTIVE
        user = (User) userService.createUser(user, null)
        log.info("User $user.firstName $user.lastName created")
        return user
    }

    Case createCase(String title, PetriNet net, LoggedUser user) {
        return workflowService.createCase(net.getStringId(), title, "", user).getCase()
    }

    Case createCase(String title, PetriNet net) {
        return createCase(title, net, ActorTransformer.toLoggedUser(userService.getSystem()))
    }

    Case createCaseAsSuper(String title, PetriNet net) {
        return createCase(title, net, superCreator.getLoggedSuper() ?: ActorTransformer.toLoggedUser(userService.getSystem()))
    }

    // TODO remove deprecated classes and methods
//    @Deprecated
//    boolean createCaseFilter(String title, String query, MergeFilterOperation operation, LoggedUser user) {
//        return filterService.saveFilter(new CreateFilterBody(title, Filter.VISIBILITY_PUBLIC, "This filter was created automatically for testing purpose only.", Filter.TYPE_TASK, query), operation, user)
//    }

    AssignTaskEventOutcome assignTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.assignTask(author, getTaskId(taskTitle, caseId))
    }

    AssignTaskEventOutcome assignTaskToSuper(String taskTitle, String caseId) {
        return assignTask(taskTitle, caseId, superCreator.loggedSuper ?: ActorTransformer.toLoggedUser(userService.getSystem()))
    }

    FinishTaskEventOutcome finishTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.finishTask(author, getTaskId(taskTitle, caseId))
    }

    FinishTaskEventOutcome finishTaskAsSuper(String taskTitle, String caseId) {
        return finishTask(taskTitle, caseId, superCreator.loggedSuper ?: ActorTransformer.toLoggedUser(userService.getSystem()))
    }

    CancelTaskEventOutcome cancelTask(String taskTitle, String caseId, LoggedUser user) {
        return taskService.cancelTask(user, getTaskId(taskTitle, caseId))
    }

    CancelTaskEventOutcome cancelTaskAsSuper(String taskTitle, String caseId) {
        return cancelTask(taskTitle, caseId, superCreator.loggedSuper ?: ActorTransformer.toLoggedUser(userService.getSystem()))
    }

    String getTaskId(String taskTitle, String caseId) {
        List<TaskReference> references = taskService.findAllByCase(caseId, null)
        return references.find { it.getTitle() == taskTitle }.stringId
    }

    SetDataEventOutcome setTaskData(String taskId, Map<String, Map<String, String>> data) {
        ObjectNode dataSet = populateDataset(data)
        dataService.setData(taskId, dataSet)
    }

    SetDataEventOutcome setTaskData(String taskTitle, String caseId, Map<String, Map<String, String>> data) {
        setTaskData(getTaskId(taskTitle, caseId), data)
    }

    List<Field> getTaskData(String taskTitle, String caseId) {
        return dataService.getData(getTaskId(taskTitle, caseId)).getData()
    }

    void updateSuperUser() {
        superCreator.setAllToSuperUser()
    }

    Optional<PetriNet> importProcessOnce(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getDefaultVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return Optional.of(filter)
        }

        Optional<PetriNet> filterNet = this.createNet(netFileName, VersionType.MAJOR, ActorTransformer.toLoggedUser(userService.getSystem()))

        if (!filterNet.isPresent()) {
            log.error("Import of ${message} failed!")
        }

        return filterNet
    }

    static ObjectNode populateDataset(Map<String, Map<String, String>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(data)
        return mapper.readTree(json) as ObjectNode
    }

    static ObjectNode populateDatasetWithObject(Map<String, Map<String, Object>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(data)
        return mapper.readTree(json) as ObjectNode
    }

    static String getCaseColor() {
        return "color-fg-amber-500"
    }
}
