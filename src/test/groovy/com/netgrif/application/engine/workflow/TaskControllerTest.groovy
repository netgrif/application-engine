package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.Authority
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.User
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.auth.service.AuthorityService
import com.netgrif.auth.service.UserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.core.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.core.workflow.domain.Case
import com.netgrif.core.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.TaskSearchService
import com.netgrif.application.engine.workflow.service.TaskService
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.TaskController
import com.netgrif.application.engine.workflow.web.WorkflowController
import com.netgrif.core.workflow.web.requestbodies.TaskSearchRequest
import com.netgrif.core.petrinet.domain.roles.ProcessRole
import com.netgrif.core.workflow.web.responsebodies.TaskReference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.mock.web.MockMultipartFile
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

    private PetriNet net

    private Case useCase

    private ProcessRole role

    private Task task

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        userService.saveUser(new com.netgrif.adapter.auth.domain.User(
                firstName: "Dummy",
                lastName: "Netgrif",
                username: DUMMY_USER_MAIL,
                email: DUMMY_USER_MAIL,
                password: "superAdminPassword",
                state: UserState.ACTIVE,
                authorities: [authorityService.getOrCreate(Authority.user)] as Set<Authority>,
                processRoles: [] as Set<ProcessRole>), null)
        importNet()
    }

    @Test
    void fullTest() {
        testWithRoleAndUserref()
        testWithUserref()
        testWithRole()
    }

    @Test
    void testDeleteFile() {
        Case testCase = helper.createCase("My case", net)
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
        Case testCase = helper.createCase("My case", net)
        String taskId = testCase.tasks.find {it.transition == "1"}.task

        dataService.saveFiles(taskId, "fileList", new MockMultipartFile[] {new MockMultipartFile("test", "test", null, new byte[] {})})
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet["fileList"].value != null

        taskController.deleteNamedFile(taskId, "fileList", "test")
        testCase = workflowService.findOne(testCase.stringId)
        assert ((FileListFieldValue) testCase.dataSet["fileList"].value).namesPaths == null || ((FileListFieldValue) testCase.dataSet["fileList"].value).namesPaths.size() == 0
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

    void importNet() {
        PetriNet netOptional = helper.createNet("all_data_refs.xml", VersionType.MAJOR).get()
        assert netOptional != null
        net = netOptional
    }

    void createCase() {
        useCase = null
        useCase = helper.createCase("My case", net)
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
        List<ProcessRole> roles = processRoleService.findAll(net.stringId)

        for (ProcessRole role : roles) {
            if (role.importId == "process_role") {
                this.role = role
            }
        }
        processRoleService.assignRolesToUser(userService.findUserByUsername(DUMMY_USER_MAIL, null).get().getStringId(), [role._id] as Set, userService.transformToLoggedUser(userService.getLoggedOrSystem()))
    }

    Page<Task> findTasksByMongo() {
        List<TaskSearchRequest> taskSearchRequestList = new ArrayList<>()
        taskSearchRequestList.add(new TaskSearchRequest())
        Page<Task> tasks = taskService.search(taskSearchRequestList, new FullPageRequest(), userService.transformToLoggedUser(userService.findUserByUsername(DUMMY_USER_MAIL, null).get()), Locale.ENGLISH, false)
        return tasks
    }
}
