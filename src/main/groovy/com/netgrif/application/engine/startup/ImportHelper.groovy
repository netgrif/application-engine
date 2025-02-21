package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.domain.*
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.DataRef
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.service.ProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Slf4j
@Component
class ImportHelper {

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private IUserService userService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ResourceLoader resourceLoader

    @Autowired(required = false)
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private INextGroupService groupService

    @Autowired
    private ProcessRoleService roleService

    @Autowired
    private IUriService uriService

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

    Optional<Process> createNet(String fileName, String release, LoggedUser author = userService.getSystem().transformToLoggedUser(), String uriNodeId = uriService.getRoot().stringId) {
        return createNet(fileName, VersionType.valueOf(release.trim().toUpperCase()), author, uriNodeId)
    }

    Optional<Process> createNet(String fileName, VersionType release = VersionType.MAJOR, LoggedUser author = userService.getSystem().transformToLoggedUser(), String uriNodeId = uriService.getRoot().stringId) {
        InputStream netStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        Process petriNet = petriNetService.importPetriNet(netStream, release, author, uriNodeId).getNet()
        log.info("Imported '${petriNet?.title?.defaultValue}' ['${petriNet?.identifier}', ${petriNet?.stringId}]")
        return Optional.of(petriNet)
    }

    Optional<Process> upsertNet(String filename, String identifier, VersionType release = VersionType.MAJOR, LoggedUser author = userService.getSystem().transformToLoggedUser()) {
        Process petriNet = petriNetService.getNewestVersionByIdentifier(identifier)
        if (!petriNet) {
            return createNet(filename, release, author)
        }
        return Optional.of(petriNet)
    }

    IUser createUser(User user, Authority[] authorities, ProcessRole[] roles) {
        authorities.each { user.addAuthority(it) }
        roles.each { user.addRole(it) }
        user.state = UserState.ACTIVE
        user = userService.saveNew(user)
        log.info("User $user.name $user.surname created")
        return user
    }

    Case createCase(String title, Process net, LoggedUser user) {
        return workflowService.createCase(net.getStringId(), title, "", user).getCase()
    }

    Case createCase(String title, Process net) {
        return createCase(title, net, userService.getSystem().transformToLoggedUser())
    }

    Case createCaseAsSuper(String title, Process net) {
        return createCase(title, net, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    AssignTaskEventOutcome assignTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.assignTask(author, getTaskId(taskTitle, caseId))
    }

    AssignTaskEventOutcome assignTaskToSuper(String taskTitle, String caseId) {
        return assignTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    FinishTaskEventOutcome finishTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.finishTask(author, getTaskId(taskTitle, caseId))
    }

    FinishTaskEventOutcome finishTaskAsSuper(String taskTitle, String caseId) {
        return finishTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    CancelTaskEventOutcome cancelTask(String taskTitle, String caseId, LoggedUser user) {
        return taskService.cancelTask(user, getTaskId(taskTitle, caseId))
    }

    CancelTaskEventOutcome cancelTaskAsSuper(String taskTitle, String caseId) {
        return cancelTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    String getTaskId(String taskTitle, String caseId) {
        List<TaskReference> references = taskService.findAllByCase(caseId, null)
        return references.find { it.getTitle() == taskTitle }.stringId
    }

    SetDataEventOutcome setTaskData(String taskId, DataSet dataSet) {
        dataService.setData(taskId, dataSet, superCreator.getSuperUser())
    }

    SetDataEventOutcome setTaskData(String taskTitle, String caseId, DataSet data) {
        setTaskData(getTaskId(taskTitle, caseId), data)
    }

    List<DataRef> getTaskData(String taskTitle, String caseId) {
        return dataService.getData(getTaskId(taskTitle, caseId), superCreator.getSuperUser()).getData()
    }

    void updateSuperUser() {
        superCreator.setAllToSuperUser();
    }
}
