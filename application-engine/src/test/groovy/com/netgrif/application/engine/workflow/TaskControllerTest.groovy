package com.netgrif.application.engine.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.DataField
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.security.service.SecurityContextService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.service.TaskSearchService
import com.netgrif.application.engine.workflow.service.TaskService
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.TaskController
import com.netgrif.application.engine.workflow.web.WorkflowController
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TaskControllerTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"

    @Autowired
    private TaskService taskService

    @Autowired
    private TaskSearchService taskSearchService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private WorkflowController workflowController

    @Autowired
    private ProcessRoleService processRoleService

    @Autowired
    private IDataService dataService

    @Autowired
    private UserService userService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private AuthorityService authorityService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TaskController taskController

    private PetriNet allDataNet

    private PetriNet setDataNet

    private Case useCase

    private ProcessRole role

    private Task task

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        def user = new User(
                firstName: "Dummy",
                lastName: "Netgrif",
                username: DUMMY_USER_MAIL,
                email: DUMMY_USER_MAIL,
                password: "superAdminPassword",
                state: UserState.ACTIVE,
                authoritySet: [authorityService.getOrCreate(Authority.user), authorityService.getOrCreate(Authority.admin)] as Set<Authority>,
                processRoles: [] as Set<ProcessRole>)

        user = userService.saveUser(user)

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(user), ActorTransformer.toLoggedUser(user).getPassword(), ActorTransformer.toLoggedUser(user).getAuthoritySet() as Set<AuthorityImpl>);
        SecurityContextHolder.getContext().setAuthentication(token)

        importNets()
    }

    @Test
    void fullTest() {
        testWithRoleAndUserref()
        testWithUserref()
        testWithRole()
    }

    @Test
    void testDeleteFile() {
        Case testCase = helper.createCase("My case", allDataNet)
        String taskId = testCase.tasks.find {it.transition == "1"}.task

        dataService.saveFile(taskId, "file", new MockMultipartFile("test", new byte[] {}))
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet["file"].value != null

        taskController.deleteFile(taskId, "file")
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet["file"].value == null
    }

    @Test
    void testDeleteFileByName() {
        Case testCase = helper.createCase("My case", allDataNet)
        String taskId = testCase.tasks.find {it.transition == "1"}.task

        dataService.saveFiles(taskId, "fileList", new MockMultipartFile[] {new MockMultipartFile("test", "test", null, new byte[] {})})
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet["fileList"].value != null

        taskController.deleteNamedFile(taskId, "fileList", "test")
        testCase = workflowService.findOne(testCase.stringId)
        assert ((FileListFieldValue) testCase.dataSet["fileList"].value).namesPaths == null || ((FileListFieldValue) testCase.dataSet["fileList"].value).namesPaths.size() == 0
    }

    @Test
    void testSetDataFieldTypeRestriction() {
        Case testCase = helper.createCase("My case", setDataNet)
        String taskId = testCase.tasks.find { it.transition == "testSetDataFieldTypeRestriction" }.task

        ObjectNode dataSet = populateNestedDataset([(taskId):["taskRef_0": ["type": "taskRef", "value": [taskId]]]])
        def response = taskController.setData(taskId, dataSet, Locale.default)
        assert response != null && response.content.outcome != null
        assert response.content.outcome.changedFields.changedFields.isEmpty()
        assert ((List<String>) workflowService.findOne(testCase.stringId).getDataField("taskRef_0").getValue()).isEmpty()

        dataSet = populateNestedDataset([(taskId):["caseRef_0": ["type": "caseRef", "value": [testCase.stringId]]]])
        response = taskController.setData(taskId, dataSet, Locale.default)
        assert response != null && response.content.outcome != null
        assert response.content.outcome.changedFields.changedFields.isEmpty()
        assert ((List<String>) workflowService.findOne(testCase.stringId).getDataField("caseRef_0").getValue()).isEmpty()
    }

    @Test
    void testSetDataVisibleField() {
        Case testCase = helper.createCase("My case", setDataNet)
        String taskId = testCase.tasks.find { it.transition == "data" }.task

        ObjectNode dataSet = populateNestedDataset([(taskId):["text_1": ["type": "text", "value": "awd"]]])
        def response = taskController.setData(taskId, dataSet, Locale.default)
        assert response != null && response.content.outcome == null
        assert response.content.error != null

        // todo: test visible behavior based on parent taskRef behavior
    }

    @Test
    void testSetDataNestedTaskRefRestrictions() {
        Case testCase1 = helper.createCase("testCase1", setDataNet)
        String taskId = testCase1.tasks.find { it.transition == "testSetDataNestedTaskRefRestrictions" }.task
        Case testCase2 = helper.createCase("testCase2", setDataNet)
        Case testCase3 = helper.createCase("testCase3", setDataNet)

        DataField case1DataField = testCase1.getDataField("taskRef_0")
        case1DataField.setValue(List.of(testCase2.tasks.find { it.transition == "testSetDataNestedTaskRefRestrictions" }.task))
        workflowService.save(testCase1)

        DataField case2DataField = testCase2.getDataField("taskRef_0")
        case2DataField.setValue(List.of(testCase3.tasks.find { it.transition == "data" }.task))
        workflowService.save(testCase2)

        String nestedOtherTaskId = testCase2.tasks.find { it.transition == "data" }.task
        ObjectNode dataSet = populateNestedDataset([(nestedOtherTaskId):["text_0": ["type": "text", "value": "awd"]]])
        def response = taskController.setData(taskId, dataSet, Locale.default)
        assert response != null && response.content.outcome != null
        assert response.content.outcome.changedFields.changedFields.isEmpty()
        assert workflowService.findOne(testCase2.stringId).getDataField("text_0").getValue() == null

        String nestedTaskId = testCase3.tasks.find { it.transition == "data" }.task
        dataSet = populateNestedDataset([(nestedTaskId):["text_0": ["type": "text", "value": "awd"]]])
        response = taskController.setData(taskId, dataSet, Locale.default)
        assert response != null && response.content.outcome != null
        assert !response.content.outcome.changedFields.changedFields.isEmpty()
        assert workflowService.findOne(testCase3.stringId).getDataField("text_0").getValue() == "awd"
    }

    @Test
    void testSetDataNonReferencedField() {
        Case testCase = helper.createCase("My case", setDataNet)
        String taskId = testCase.tasks.find { it.transition == "testSetDataNonReferencedField" }.task

        ObjectNode dataSet = populateNestedDataset([(taskId):["text_1": ["type": "text", "value": "awd"]]])
        def response = taskController.setData(taskId, dataSet, Locale.default)

        assert response != null && response.content.outcome == null
        assert response.content.error != null
    }


    void testWithRoleAndUserref() {
        createCase()
        findTask()
        setUserListValue()
        setUserRole()
        assert !findTasksByMongo().empty
    }

    void testWithRole() {
        createCase()
        findTask()
        setUserRole()
        assert !findTasksByMongo().empty
    }

    void testWithUserref() {
        createCase()
        findTask()
        setUserListValue()
        assert !findTasksByMongo().empty
    }

    void importNets() {
        PetriNet allDataNet = helper.createNet("all_data_refs.xml", VersionType.MAJOR).get()
        assert allDataNet != null
        this.allDataNet = allDataNet

        PetriNet setDataNet = helper.createNet("task_controller_set_data.xml", VersionType.MAJOR).get()
        assert setDataNet != null
        this.setDataNet = setDataNet
    }

    void createCase() {
        useCase = null
        useCase = helper.createCase("My case", allDataNet)
        assert useCase != null
    }

    void findTask() {
        List<TaskReference> taskReferences = taskService.findAllByCase(useCase.stringId, new Locale("en"))
        assert taskReferences.size() > 0
        for (TaskReference tr : taskReferences) {
            if (tr.title == "Task - editable") {
                task = taskService.findById(tr.stringId)
            }
        }
        assert task != null
    }

    void setUserListValue() {
        assert task != null
        List<String> userIds = [] as List
        userIds.add(userService.findUserByUsername(DUMMY_USER_MAIL, null).get().getStringId())
        dataService.setData(task.stringId, ImportHelper.populateDataset([
                "performable_users": [
                        "value": userIds,
                        "type" : "userList"
                ]
        ]))
    }

    void setUserRole() {
        List<ProcessRole> roles = processRoleService.findAllByNetStringId(allDataNet.stringId)

        for (ProcessRole role : roles) {
            if (role.importId == "process_role") {
                this.role = role
            }
        }
        processRoleService.assignRolesToUser(userService.findUserByUsername(DUMMY_USER_MAIL, null).get(), [role._id] as Set, ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
    }

    Page<Task> findTasksByMongo() {
        List<TaskSearchRequest> taskSearchRequestList = new ArrayList<>()
        taskSearchRequestList.add(new TaskSearchRequest())
        Page<Task> tasks = taskService.search(taskSearchRequestList, new FullPageRequest(), ActorTransformer.toLoggedUser(userService.findUserByUsername(DUMMY_USER_MAIL, null).get()), Locale.ENGLISH, false)
        return tasks
    }

    static ObjectNode populateNestedDataset(Map<String, Map<String, Map<String, Object>>> data) {
        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(data)
        return mapper.readTree(json) as ObjectNode
    }
}
