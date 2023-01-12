package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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
    private IDataService dataService

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
        def userEmails = ["super@netgrif.com", "engine@netgrif.com"]
        newCases = new ArrayList<>()
        userIds = new ArrayList<>()
        10.times {
            def _case = importHelper.createCase("$it" as String, it % 2 == 0 ? net : net)
            String id = userService.findByEmail(userEmails[it % 2], true).getStringId()
            String taskId = (new ArrayList<>(_case.getTasks())).get(0).task
            _case = dataService.setData(taskId, ImportHelper.populateDataset([
                    "user_list_1": [
                            "value": [id],
                            "type": "userList"
                    ]
            ] as Map)).getCase()
            newCases.add(workflowService.save(_case))
            userIds.add(id)
        }
    }

    @Test
    void testCases() {
        newCases.eachWithIndex { Case entry, int i -> assert entry.users.get(userIds.get(i)) != null }
    }


}
