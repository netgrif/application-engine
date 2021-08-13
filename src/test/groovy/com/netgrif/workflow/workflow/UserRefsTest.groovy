package com.netgrif.workflow.workflow

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.utils.FullPageRequest
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import groovy.util.logging.Slf4j
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
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
    private TestHelper helper

    List<Case> newCases

    List<Long> userIds

    private String netId

    @Before
    void before() {
        helper.truncateDbs()
        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/userrefs_test.xml"), VersionType.MAJOR, userService.loggedOrSystem.transformToLoggedUser())
        assert net.isPresent()
        netId = net.get().getStringId()
        def userEmails = ["super@netgrif.com", "engine@netgrif.com"]
        newCases = new ArrayList<>()
        userIds = new ArrayList<>()
        10.times {
            def _case = importHelper.createCase("$it" as String, it % 2 == 0 ? net.get() : net.get())
            long id = userService.findByEmail(userEmails[it % 2], true).id
            _case.dataSet["user_list_1"].value = [id]
            newCases.add(workflowService.save(_case))
            userIds.add(id)
        }
    }

    @Test
    void testCases() {
        newCases.eachWithIndex { Case entry, int i -> assert entry.users.get(userIds.get(i)) != null }
    }


}
