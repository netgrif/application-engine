package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.domain.Role
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
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
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IIdentityService identityService

    @Autowired
    private TaskController taskController

    private Process net

    private Case useCase

    private Role role

    private Task task

    private Identity testIdentity

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        testIdentity = helper.createIdentity(IdentityParams.with()
                .firstname(new TextField("Dummy"))
                .lastname(new TextField("Netgrif"))
                .username(new TextField(DUMMY_USER_MAIL))
                .password(new TextField("superAdminPassword"))
                .build(), new ArrayList<Role>())
        importNet()
        testHelper.login(superCreator.superIdentity)
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
        setActorRole()
        assert !findTasksByMongo().empty
    }

    void testWithRole() {
        createCase()
        findTask()
        setActorRole()
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

    void setUserListValue() {
        assert task != null
        // TODO: release/8.0.0 field 'performable_users' does not exist
//        String userId = userService.findByEmail(DUMMY_USER_MAIL).getStringId()
//        dataService.setData(task.stringId, new DataSet([
//                "performable_users": new UserListField(rawValue: new UserListFieldValue(dataService.makeUserFieldValue(userId)))
//        ] as Map<String, Field<?>>))
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

    void setActorRole() {
        this.role = roleService.findProcessRoleByImportId("process_role")
        roleService.assignRolesToActor(testIdentity.toSession().activeActorId, [role.id.toString()] as Set)
    }

    Page<Task> findTasksByMongo() {
        List<TaskSearchRequest> taskSearchRequestList = new ArrayList<>()
        taskSearchRequestList.add(new TaskSearchRequest())
        String actorId = testIdentity.toSession().activeActorId
        Page<Task> tasks = taskService.search(taskSearchRequestList, new FullPageRequest(), actorId, new Locale("en"), false)
        return tasks
    }
}
