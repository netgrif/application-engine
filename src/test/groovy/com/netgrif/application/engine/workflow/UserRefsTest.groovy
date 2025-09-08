package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.UserListField
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.params.SetDataParams
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
    private IIdentityService identityService

    @Autowired
    private TestHelper helper

    List<Case> newCases

    List<String> actorIds

    private String netId

    @BeforeEach
    void before() {
        // todo release/8.0.0 userList is already tested in RoleServiceTest
        helper.truncateDbs()
        helper.login(superCreator.superIdentity)
        def net = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/userrefs_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().activeActorId)).getProcess()
        assert net
        netId = net.getStringId()
        def userEmails = [configuration.email, "engine@netgrif.com"]
        newCases = new ArrayList<>()
        actorIds = new ArrayList<>()
        10.times {
            def _case = importHelper.createCase("$it" as String, net)
            Identity identity = identityService.findByUsername(userEmails[it % 2]).get()
            String actorId = identity.toSession().activeActorId
            String taskId = _case.getTaskStringId("t1")
            dataService.setData(new SetDataParams(taskId, new DataSet([
                    "user_list_1": new UserListField(rawValue: new UserListFieldValue([dataService.makeUserFieldValue(actorId)]))
            ] as LinkedHashMap<String, Field<?>>), superCreator.getLoggedSuper().activeActorId)).getCase()
            actorIds.add(actorId)
        }
    }

    @Test
    void testCases() {
        // TODO: release/8.0.0 fix?
//        newCases.eachWithIndex { Case entry, int i -> assert entry.users.get(userIds.get(i)) != null }
    }
}
