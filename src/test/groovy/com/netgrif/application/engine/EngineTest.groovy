package com.netgrif.application.engine

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
abstract class EngineTest {
    @Autowired
    public ImportHelper importHelper
    @Autowired
    public TestHelper testHelper
    @Autowired
    public SuperCreator superCreator
    @Autowired
    public WebApplicationContext wac
    @Autowired
    public ITaskService taskService
    @Autowired
    public IProcessRoleService roleService
    @Autowired
    public IUserService userService
    @Autowired
    public IWorkflowService workflowService
    @Autowired
    public IPetriNetService petriNetService
    @Autowired
    public PetriNetRepository processRepository

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }
}