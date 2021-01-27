package com.netgrif.workflow.workflow

import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.elastic.service.ElasticTaskService
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.service.ProcessRoleService;
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.utils.FullPageRequest
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.MergeFilterOperation
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.DataService;
import com.netgrif.workflow.workflow.service.TaskSearchService
import com.netgrif.workflow.workflow.service.TaskService;
import com.netgrif.workflow.workflow.service.TaskServiceTest;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.web.WorkflowController
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import groovy.util.logging.Slf4j
import org.apache.tomcat.jni.Local
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles(["dev"])
@RunWith(SpringRunner.class)
class TaskControllerTest {

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
    private IUserService userService

    @Autowired
    private ImportHelper helper

    private PetriNet net

    private Case useCase

    private ProcessRole role

    private Task task

    @Before
    void init(){
        importNet()
    }

    @Test
    void fullTest(){
        testWithRoleAndUserref()
        testWithUserref()
        testWithRole()
    }


    void testWithRoleAndUserref(){
        createCase()
        findTask()
        setUserListValue()
        setUserRole()
        assert !findTasksByMongo().empty
    }

    void testWithRole(){
        createCase()
        findTask()
        setUserRole()
        assert !findTasksByMongo().empty
    }

    void testWithUserref(){
        createCase()
        findTask()
        setUserListValue()
        assert !findTasksByMongo().empty
    }

    void importNet() {
        Optional<PetriNet> netOptional = helper.createNet("all_data.xml", "major")
        assert netOptional.isPresent()
        net = netOptional.get()
    }

    void createCase() {
        useCase = null
        useCase = helper.createCase("My case", net)
        assert useCase != null
    }

    void findTask() {
        List<TaskReference> taskReferences = taskService.findAllByCase(useCase.stringId, new Locale("en"))
        assert taskReferences.size() > 0
        for (TaskReference tr: taskReferences) {
            if (tr.title == "Task - editable") {
                task = taskService.findById(tr.stringId)
            }
        }
        assert task != null
    }

    void setUserListValue() {
        assert task != null
        List<Long> userIds = [] as List
        userIds.add(userService.findByEmail("dummy@netgrif.com", false).getId())
        dataService.setData(task.stringId,  ImportHelper.populateDataset([
                "performable_users": [
                        "value": userIds,
                        "type": "userList"
                ]
        ]))
    }

    void setUserRole() {
        List<ProcessRole> roles = processRoleService.findAll(net.stringId)

        for (ProcessRole role : roles) {
            if (role.importId == "dummy") {
                this.role = role
            }
        }
        processRoleService.assignRolesToUser(userService.findByEmail("dummy@netgrif.com", false).getId(), [role._id.toString()] as Set, userService.getLoggedOrSystem().transformToLoggedUser())
    }

    Page<Task> findTasksByMongo() {
        List<TaskSearchRequest> taskSearchRequestList = new ArrayList<>()
        taskSearchRequestList.add(new TaskSearchRequest())
        Page<Task> tasks = taskService.search(taskSearchRequestList, new FullPageRequest(), userService.findByEmail("dummy@netgrif.com", false).transformToLoggedUser(), new Locale("en"), false)
        return tasks
    }
}
