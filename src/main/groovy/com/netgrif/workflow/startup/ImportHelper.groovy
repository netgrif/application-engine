package com.netgrif.workflow.startup

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.workflow.auth.domain.*
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.service.IGroupService
import com.netgrif.workflow.orgstructure.service.IMemberService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.EventOutcome
import com.netgrif.workflow.workflow.domain.Filter
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IFilterService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference
import groovy.json.JsonOutput
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
    private IUserService userService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ResourceLoader resourceLoader

    @Autowired
    private IFilterService filterService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IGroupService groupService

    @Autowired
    private IMemberService memberService

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    private final ClassLoader loader = ImportHelper.getClassLoader()

    @SuppressWarnings("GroovyAssignabilityCheck")
    Map<String, Group> createGroups(Map<String, String> groups) {
        HashMap<String, Group> groupsMap = new HashMap<>()
        groups.each { groupEntry ->
            groupsMap.put(groupEntry.key, createGroup(groupEntry.value))
        }

        log.info("Created ${groupsMap.size()} groups")
        return groupsMap
    }

    Group createGroup(String name) {
        log.info("Creating Group $name")
        return groupService.save(new Group(name))
    }

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

    Optional<PetriNet> createNet(String fileName, String release) {
        createNet(fileName, release, superCreator.loggedSuper)
    }

    Optional<PetriNet> createNet(String fileName, String release, LoggedUser loggedUser) {
        InputStream netStream = new ClassPathResource("petriNets/$fileName" as String).inputStream
        return petriNetService.importPetriNet(netStream, release, loggedUser)
    }

    UserProcessRole createUserProcessRole(PetriNet net, String name) {
        UserProcessRole role = userProcessRoleRepository.save(new UserProcessRole(roleId:
                net.roles.values().find { it -> it.name.defaultValue == name }.stringId, netId: net.getStringId()))
        log.info("Created user process role $name")
        return role
    }

    Map<String, UserProcessRole> createUserProcessRoles(Map<String, String> roles, PetriNet net) {
        HashMap<String, UserProcessRole> userRoles = new HashMap<>()
        roles.each { it ->
            userRoles.put(it.key, createUserProcessRole(net, it.value))
        }

        log.info("Created ${userRoles.size()} process roles")
        return userRoles
    }

    Map<String, UserProcessRole> getProcessRoles(PetriNet net) {
        List<UserProcessRole> roles = userProcessRoleRepository.findAllByNetId(net.stringId)
        Map<String, UserProcessRole> map = [:]
        net.roles.values().each { netRole ->
            map[netRole.name.getDefaultValue()] = roles.find { it.roleId == netRole.stringId }
        }
        return map
    }

    User createUser(User user, Authority[] authorities, Group[] orgs, UserProcessRole[] roles) {
        authorities.each { user.addAuthority(it) }
        roles.each { user.addProcessRole(it) }
        user.groups = orgs as Set
        user.state = UserState.ACTIVE
        user = userService.saveNew(user)
        log.info("User $user.name $user.surname created")
        return user
    }

    Case createCase(String title, PetriNet net, LoggedUser user) {
        return workflowService.createCase(net.getStringId(), title, "", user)
    }

    Case createCase(String title, PetriNet net) {
        return createCase(title, net, superCreator.loggedSuper)
    }

    boolean createFilter(String title, String query, String readable, LoggedUser user) {
        return filterService.saveFilter(new CreateFilterBody(title, Filter.VISIBILITY_PUBLIC, "This filter was created automatically for testing purpose only.", Filter.TYPE_TASK, query, readable), user)
    }

    EventOutcome assignTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.assignTask(author, getTaskId(taskTitle, caseId))
    }

    EventOutcome assignTaskToSuper(String taskTitle, String caseId) {
        return assignTask(taskTitle, caseId, superCreator.loggedSuper)
    }

    EventOutcome finishTask(String taskTitle, String caseId, LoggedUser author) {
        return taskService.finishTask(author, getTaskId(taskTitle, caseId))
    }

    EventOutcome finishTaskAsSuper(String taskTitle, String caseId) {
        return finishTask(taskTitle, caseId, superCreator.loggedSuper)
    }

    EventOutcome cancelTask(String taskTitle, String caseId, LoggedUser user) {
        return taskService.cancelTask(user, getTaskId(taskTitle, caseId))
    }

    EventOutcome cancelTaskAsSuper(String taskTitle, String caseId) {
        return cancelTask(taskTitle, caseId, superCreator.loggedSuper)
    }

    String getTaskId(String taskTitle, String caseId) {
        List<TaskReference> references = taskService.findAllByCase(caseId, null)
        return references.find { it.getTitle() == taskTitle }.stringId
    }

    ChangedFieldContainer setTaskData(String taskId, Map<String, Map<String,String>> data) {
        ObjectNode dataSet = populateDataset(data)
         dataService.setData(taskId, dataSet)
    }

    ChangedFieldContainer setTaskData(String taskTitle, String caseId, Map<String, Map<String,String>> data) {
        setTaskData(getTaskId(taskTitle, caseId), data)
    }

    List<Field> getTaskData(String taskTitle, String caseId) {
        return dataService.getData(getTaskId(taskTitle, caseId))
    }

    void updateSuperUser(){
        superCreator.setAllToSuperUser();
    }

    static ObjectNode populateDataset(Map<String, Map<String, String>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = JsonOutput.toJson(data)
        return mapper.readTree(json) as ObjectNode
    }

    static String getCaseColor() {
        return "color-fg-amber-500"
    }
}