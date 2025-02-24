package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.TaskPair
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@Slf4j
@CompileStatic
class UserRefsTest {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IUserService userService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private SuperAdminConfiguration configuration

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper helper

    List<Case> newCases

    List<String> userIds

    private String netId

    @BeforeEach
    void before() {
        helper.truncateDbs()
        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/userrefs_test.xml"), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser()).getNet()
        assert net
        netId = net.getStringId()
        def userEmails = [configuration.email, "engine@netgrif.com"]
        newCases = new ArrayList<>()
        userIds = new ArrayList<>()
        10.times {
            def _case = importHelper.createCase("$it" as String, net)
            String id = userService.findByEmail(userEmails[it % 2]).getStringId()
            String taskId = _case.getTaskStringId("t1")
            dataService.setData(taskId, new DataSet([
                    "user_list_1": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(id)]))
            ] as Map<String, Field<?>>), superCreator.getLoggedSuper()).getCase()
            userIds.add(id)
        }
    }

    @Test
    void testCases() {
        // TODO: release/8.0.0 fix?
//        newCases.eachWithIndex { Case entry, int i -> assert entry.users.get(userIds.get(i)) != null }
    }
}
