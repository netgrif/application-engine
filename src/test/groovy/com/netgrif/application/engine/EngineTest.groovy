package com.netgrif.application.engine

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
abstract class EngineTest {
    ImportHelper importHelper
    WebApplicationContext wac
    TestHelper testHelper
    ITaskService taskService
    IProcessRoleService roleService
    IUserService userService
    IWorkflowService workflowService

    @Autowired
    EngineTest(ImportHelper importHelper, WebApplicationContext wac, TestHelper testHelper, ITaskService taskService, IProcessRoleService roleService, IUserService userService, IWorkflowService workflowService) {
        this.importHelper = importHelper
        this.wac = wac
        this.testHelper = testHelper
        this.taskService = taskService
        this.roleService = roleService
        this.userService = userService
        this.workflowService = workflowService
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }
}
