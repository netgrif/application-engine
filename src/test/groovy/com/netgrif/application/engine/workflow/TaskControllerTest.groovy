package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.IdentityState


import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.TaskSearchService
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.TaskController
import com.netgrif.application.engine.workflow.web.WorkflowController
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference
import groovy.transform.CompileStatic
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
@CompileStatic
class TaskControllerTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"

    @Autowired
    private ITaskService taskService

    @Autowired
    private TaskSearchService taskSearchService

    @Autowired
    private IElasticTaskService elasticTaskService

    @Autowired
    private WorkflowController workflowController

    @Autowired
    private IRoleService roleService

    @Autowired
    private IDataService dataService

    @Autowired
    private IUserService userService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TaskController taskController

    private Process net

    private Case useCase

    private Role role

    private Task task

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        userService.saveNew(new User(
                name: "Dummy",
                surname: "Netgrif",
                email: DUMMY_USER_MAIL,
                password: "superAdminPassword",
                state: IdentityState.ACTIVE,
                authorities: [authorityService.getOrCreate(SessionRole.user)] as Set<SessionRole>))
                // todo 2058
//                roles: [] as Set<ProcessRole>))
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
        String taskId = testCase.getTaskStringId("t1")

        // TODO: release/8.0.0
// java.lang.NullPointerException: Cannot invoke "com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue.getName()" because the return value of "com.netgrif.application.engine.workflow.domain.DataFieldValue.getValue()" is null
        dataService.saveFile(taskId, "file", new MockMultipartFile("test", new byte[]{}))
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet.get("file").rawValue != null

        taskController.deleteFile(taskId, "file")
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet.get("file").rawValue == null
    }

    @Test
    void testDeleteFileByName() {
        Case testCase = helper.createCase("My case", net)
        String taskId = testCase.getTaskStringId( "t1")

        dataService.saveFiles(taskId, "fileList", new MockMultipartFile[]{new MockMultipartFile("test", "test", null, new byte[]{})})
        testCase = workflowService.findOne(testCase.stringId)
        assert testCase.dataSet.get("fileList").rawValue != null

        taskController.deleteNamedFile(taskId, "fileList", "test")
        testCase = workflowService.findOne(testCase.stringId)
        assert ((FileListFieldValue) testCase.dataSet.get("fileList").rawValue).namesPaths == null ||
                ((FileListFieldValue) testCase.dataSet.get("fileList").rawValue).namesPaths.size() == 0
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
        Process netOptional = helper.createNet("all_data_refs.xml", VersionType.MAJOR).get()
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
        String userId = userService.findByEmail(DUMMY_USER_MAIL).getStringId()
        // TODO: release/8.0.0 field 'performable_users' does not exist
//        dataService.setData(task.stringId, new DataSet([
//                "performable_users": new UserListField(rawValue: new UserListFieldValue(dataService.makeUserFieldValue(userId)))
//        ] as Map<String, Field<?>>))
    }

    void setUserRole() {
        List<Role> roles = roleService.findAll()

        for (Role role : roles) {
            if (role.importId == "process_role") {
                this.role = role
            }
        }
        roleService.assignRolesToActor(userService.findByEmail(DUMMY_USER_MAIL).getStringId(), [role.id.toString()] as Set)
    }

    Page<Task> findTasksByMongo() {
        List<TaskSearchRequest> taskSearchRequestList = new ArrayList<>()
        taskSearchRequestList.add(new TaskSearchRequest())
        Page<Task> tasks = taskService.search(taskSearchRequestList, new FullPageRequest(), userService.findByEmail(DUMMY_USER_MAIL).transformToLoggedUser(), new Locale("en"), false)
        return tasks
    }
}
