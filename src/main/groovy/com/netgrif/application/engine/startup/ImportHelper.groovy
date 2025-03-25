package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.*
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import com.netgrif.application.engine.authorization.domain.ProcessRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.DataRef
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
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

import java.util.stream.Collectors

@Slf4j
@Component
class ImportHelper {

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private IUserService userService

    @Autowired
    private IIdentityService identityService

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
    private IRoleService roleService

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

    Optional<Process> createNet(String fileName, String release, LoggedIdentity author = identityService.loggedIdentity,
                                String uriNodeId = uriService.getRoot().stringId) {
        return createNet(fileName, VersionType.valueOf(release.trim().toUpperCase()), author, uriNodeId)
    }

    Optional<Process> createNet(String fileName, VersionType release = VersionType.MAJOR, LoggedIdentity author = identityService.loggedIdentity,
                                String uriNodeId = uriService.getRoot().stringId) {
        InputStream netStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        Process petriNet = petriNetService.importPetriNet(netStream, release, author.activeActorId, uriNodeId).getNet()
        log.info("Imported '${petriNet?.title?.defaultValue}' ['${petriNet?.identifier}', ${petriNet?.stringId}]")
        return Optional.of(petriNet)
    }

    Optional<Process> upsertNet(String filename, String identifier, VersionType release = VersionType.MAJOR, LoggedIdentity author = identityService.loggedIdentity) {
        Process petriNet = petriNetService.getNewestVersionByIdentifier(identifier)
        if (!petriNet) {
            return createNet(filename, release, author)
        }
        log.info("Process with identifier [{}] already exists", identifier)
        return Optional.of(petriNet)
    }

    /**
     * todo javadoc
     * */
    Identity createIdentity(IdentityParams params, Authority[] authorities, ProcessRole[] roles) {
        Identity identity = identityService.createWithDefaultActor(params)

        // todo 2058
//        authorities.each { user.addAuthority(it) }
        Set<String> roleIds = Arrays.stream(roles).map { role -> role.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToActor(identity.getMainActorId(), roleIds)
        log.info("Identity [{}][{}] created with default actor [{}].", identity.getStringId(), identity.getUsername(),
                identity.getMainActorId())

        return identity
    }

    Case createCase(String title, Process net, LoggedIdentity author) {
        return workflowService.createCase(net.getStringId(), title, "", author.activeActorId).getCase()
    }

    Case createCase(String title, Process net) {
        return createCase(title, net, identityService.loggedIdentity)
    }

    Case createCaseAsSuper(String title, Process net) {
        // todo 2058 system
        return createCase(title, net, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    AssignTaskEventOutcome assignTask(String taskTitle, String caseId, LoggedIdentity assignee) {
        return taskService.assignTask(assignee.activeActorId, getTaskId(taskTitle, caseId))
    }

    AssignTaskEventOutcome assignTaskToSuper(String taskTitle, String caseId) {
        // todo 2058 system
        return assignTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    FinishTaskEventOutcome finishTask(String taskTitle, String caseId, LoggedIdentity assignee) {
        return taskService.finishTask(assignee.activeActorId, getTaskId(taskTitle, caseId))
    }

    FinishTaskEventOutcome finishTaskAsSuper(String taskTitle, String caseId) {
        // todo 2058 system
        return finishTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    CancelTaskEventOutcome cancelTask(String taskTitle, String caseId, LoggedIdentity assignee) {
        return taskService.cancelTask(assignee.activeActorId, getTaskId(taskTitle, caseId))
    }

    CancelTaskEventOutcome cancelTaskAsSuper(String taskTitle, String caseId) {
        // todo 2058 system
        return cancelTask(taskTitle, caseId, superCreator.loggedSuper ?: userService.getSystem().transformToLoggedUser())
    }

    String getTaskId(String taskTitle, String caseId) {
        List<TaskReference> references = taskService.findAllByCase(caseId, null)
        return references.find { it.getTitle() == taskTitle }.stringId
    }

    SetDataEventOutcome setTaskData(String taskId, DataSet dataSet) {
        // todo 2058 system
        dataService.setData(taskId, dataSet, superCreator.getSuperIdentity())
    }

    SetDataEventOutcome setTaskData(String taskTitle, String caseId, DataSet data) {
        setTaskData(getTaskId(taskTitle, caseId), data)
    }

    List<DataRef> getTaskData(String taskTitle, String caseId) {
        // todo 2058 system
        return dataService.getData(getTaskId(taskTitle, caseId), superCreator.getSuperIdentity()).getData()
    }

    void updateSuperUser() {
        superCreator.setAllToSuperUser();
    }
}
